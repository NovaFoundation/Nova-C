package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.exceptionhandling.Exception;
import net.fathomsoft.nova.tree.variables.Variable;
import net.fathomsoft.nova.util.SyntaxUtils;

public abstract class MethodCallArgumentListWriter extends ArgumentListWriter
{
	public abstract MethodCallArgumentList node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateSourceFragment(builder);
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		MethodCall call = node().getMethodCall();
		
		CallableMethod method = call.getInferredDeclaration();
		
		builder.append('(');
		
		generateDefaultArguments(builder);
		
		int i = 0;
		
		Value[] values = method instanceof NovaMethodDeclaration ? node().getArgumentsInOrder((NovaMethodDeclaration)method) : node().getArgumentsInOrder();
		
		while (i < values.length)
		{
			if (i > 0)
			{
				builder.append(", ");
			}
			
			Value arg = values[i];
			Value param = method.getParameterList().getParameter(i);
			
			if (arg instanceof DefaultArgument)
			{
				DefaultArgumentWriter.generateDefaultArgumentOutput(builder, param);
			}
			else
			{
				if (method.isVirtual() && !call.isVirtualTypeKnown())
				{
					VirtualMethodDeclaration virtual = ((NovaMethodDeclaration)method).getVirtualMethod();
					
					if (virtual != null)
					{
						param = virtual.getParameter(i);
					}
				}
				
				boolean sameType = SyntaxUtils.isSameType(arg.getReturnedNode(), param, false) || param.isPrimitiveType() && arg.isPrimitiveType();
				
				if (!sameType)
				{
					Value ret = arg.getReturnedNode();
					
					getWriter(param).generateTypeCast(builder).append(getWriter(ret).generatePointerToValueConversion(param));
				}
				
				generateArgumentPrefix(builder, arg, i);
				
				if (!sameType)
				{
					builder.append('(');
				}
				
				if (param.isValueReference())
				{
					builder.append('&');
				}
				
				getWriter(arg).generateArgumentOutput(builder);
				
				if (!sameType)
				{
					builder.append(')');
				}
			}
			
			i++;
		}
		
		ParameterList params = node().getMethodDeclaration().getParameterList();
		
		while (i < params.getNumVisibleChildren())
		{
			builder.append(", ");
			
			DefaultArgumentWriter.generateDefaultArgumentOutput(builder, params.getVisibleChild(i));
			
			i++;
		}
		
		if (node().getMethodCall().getCallableDeclaration() instanceof ClosureDeclaration)
		{
			builder.append(", ").append(((ClosureDeclaration)node().getMethodCall().getCallableDeclaration()).getContextName());
		}
		
		return builder.append(')');
	}
	
	/**
	 * Generate the output of the default arguments. The default arguments
	 * may include the ExceptionData instance as well as the class
	 * instance, if it is non-static.
	 *
	 * @param builder The StringBuilder to append to.
	 * @return The appended StringBuilder instance.
	 */
	private StringBuilder generateDefaultArguments(StringBuilder builder)
	{
		if (!node().getMethodCall().isExternal())
		{
			checkReference(builder).append(Exception.EXCEPTION_DATA_IDENTIFIER);
			
			if (node().getNumChildren() > 0)
			{
				builder.append(", ");
			}
		}
		
		return builder;
	}
	
	/**
	 * Generate any data that needs to be output before the argument
	 * is generated, such as a type cast for a volatile local variable
	 * or a data type change.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @param child The Value that is being output as an argument.
	 * @param argNum The number of argument that the list is outputting.
	 * @return The StringBuilder with the appended data.
	 */
	private StringBuilder generateArgumentPrefix(StringBuilder builder, Value child, int argNum)
	{
		Value parameter = node().getMethodCall().getInferredDeclaration().getParameterList().getParameter(argNum);
		
		if (child instanceof Variable)
		{
			Variable var = (Variable)child;
			
			if (var.isVolatile())
			{
				getWriter(parameter).generateTypeCast(builder);
			}
		}
		
		if (parameter.getDataType() != child.getReturnedNode().getDataType())
		{
			if (!node().getMethodCall().getReferenceNode().toValue().isPrimitiveGenericTypeWrapper())//parameter.getArrayDimensions() == 0 || parameter.isWithinExternalContext() || parameter.getArrayDimensions() != child.getReturnedNode().getArrayDimensions())
			{
				builder.append(parameter.generateDataTypeOutput(child.getReturnedNode().getDataType()));
			}
		}
		
		return builder;
	}
	
	/**
	 * If the method call needs to pass a reference of the class instance,
	 * then generate the required argument.
	 *
	 * @param builder The StringBuilder to append to.
	 * @return The appended StringBuilder instance.
	 */
	private StringBuilder checkReference(StringBuilder builder)
	{
		CallableMethod method = node().getMethodCall().getInferredDeclaration();
		
		if (method instanceof Constructor || !node().getMethodCall().getDeclaration().isInstance())
		{
			builder.append(0);
		}
		else if (method instanceof ClosureDeclaration)
		{
			ClosureDeclaration closure = (ClosureDeclaration)method;
			
			getWriter(closure).generateObjectReferenceIdentifier(builder);
		}
		else
		{
			if (method instanceof Destructor)
			{
				builder.append('&');
			}
			
			Accessible context  = node().getMethodCallContext();
			MethodCall call     = node().getMethodCall();
			ClassDeclaration castClass = null;
			
			boolean sameType = SyntaxUtils.isSameType((Value)call.getReferenceNode(), method.getParentClass(), false);
			
			if (method.isVirtual() && !call.isVirtualTypeKnown())
			{
				castClass = ((NovaMethodDeclaration)method).getVirtualMethod().getParentClass();
			}
			else if (!sameType)
			{
				castClass = method.getParentClass();
			}
			
			if (castClass != null)
			{
				getWriter(castClass).generateTypeCast(builder, true, false).append('(');
			}
			
			// Chop off the method call so it does not get cloned over.
			Accessible accessible = context;
			
			if (accessible.doesAccess())
			{
				Accessible accessed = context.getAccessedNode();
				
				while (accessed != null && accessed != call)
				{
					accessible = accessible.getAccessedNode();
					accessed   = accessible.getAccessedNode();
				}
				
				accessible.setAccessedNode(call);
			}
			
			Accessible ref = context.getCArgumentReferenceContext();
			
			getWriter(ref).generateArgumentReference(builder, call);
			
			if (castClass != null)
			{
				builder.append(')');
			}
		}
		
		builder.append(", ");
		
		return builder;
	}
}
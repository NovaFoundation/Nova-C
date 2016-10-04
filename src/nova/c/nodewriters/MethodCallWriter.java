package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.variables.VariableDeclaration;

public abstract class MethodCallWriter extends VariableWriter
{
	public abstract MethodCall node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateSourceFragment(builder).append(';').append('\n');
	}
	
	public StringBuilder generatedCSourceFragment(StringBuilder builder, boolean checkSpecial)
	{
		if (checkSpecial && node().isSpecialFragment())
		{
			return generateSpecialFragment(builder);
		}
		
		return generateUseOutput(builder);
	}
	
	/**
	 * Generate a String representing the output of the children of the
	 * MethodCall.
	 *
	 * @return A String representing the output of the children of the
	 * 		MethodCall.
	 */
	public StringBuilder generatehildrenCSourceFragment(StringBuilder builder)
	{
		for (int i = 1; i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			builder.append("->");
			
			getWriter(child).generateSourceFragment(builder);
		}
		
		return builder;
	}
	
	/**
	 * Generate the representation of when the method call is being used
	 * in action.
	 *
	 * @return What the method call looks like when it is being used in
	 * 		action.
	 */
	public StringBuilder generateUseOutput(StringBuilder builder, boolean pointer, boolean checkAccesses)
	{
		VariableDeclaration method   = node().getMethodDeclaration();
		CallableMethod      callable = (CallableMethod)method;
		
		boolean requiresCast = checkAccesses && node().doesAccess() && node().getAccessedNode() instanceof MethodCall == false && node().isGenericType();
		
		if (requiresCast)
		{
			builder.append('(');
			generateTypeCast(builder);
		}
		
		if (callable.isVirtual() && ((NovaMethodDeclaration)method).getVirtualMethod() != null && !node().isVirtualTypeKnown())
		{
			NovaMethodDeclaration novaMethod = (NovaMethodDeclaration)method;
			
                /*if (!isAccessed())
                {
                    builder.append(ParameterList.OBJECT_REFERENCE_IDENTIFIER).append("->");
                }
                
                if (getParent() instanceof Variable)
                {
                    //((Variable)getParent()).generateUseOutput(builder).append("->");
                }
                
                builder.append(VTable.IDENTIFIER).append("->");
                
                if (method.getParentClass() instanceof Interface)
                {
                    builder.append(InterfaceVTable.IDENTIFIER).append(".");
                }*/
			
			VirtualMethodDeclaration virtual = novaMethod.getVirtualMethod();
			
			getWriter(virtual).generateSourceName(builder);
		}
		else
		{
			getWriter(method).generateSourceName(builder);
		}
		
		MethodCallArgumentList args = node().getArgumentList();
		
		getWriter(args).generateSource(builder);
		
		if (requiresCast)
		{
			builder.append(')');
		}
		
		return builder;
	}
	
	public StringBuilder generateExtraArguments(StringBuilder builder)
	{
		return builder;
	}
	
	public StringBuilder generateObjectReferenceIdentifier(StringBuilder builder)
	{
		return builder;
	}
}
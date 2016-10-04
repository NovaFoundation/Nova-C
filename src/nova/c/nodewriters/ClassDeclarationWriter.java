package nova.c.nodewriters;

import net.fathomsoft.nova.Nova;
import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.Package;
import net.fathomsoft.nova.tree.variables.FieldList;
import net.fathomsoft.nova.tree.variables.InstanceFieldList;

public abstract class ClassDeclarationWriter extends InstanceDeclarationWriter
{
	public abstract ClassDeclaration node();
	
	/**
	 * Generate the C output for when node() value node is being used
	 * as an argument for a method call.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @param callingMethod The method that is being called by the
	 * 		specified Identifier.
	 * @return The C output for when node() value node is being used
	 * 		as an argument for a method call.
	 */
	public StringBuilder generateArgumentReference(StringBuilder builder, Identifier callingMethod)
	{
		if (callingMethod instanceof MethodCall)
		{
			CallableMethod declaration = ((MethodCall)callingMethod).getInferredDeclaration();
			
			if (declaration.isStatic() || declaration instanceof Constructor)
			{
				Parameter ref = declaration.getParameterList().getObjectReference();
				
				return ref.getTarget().generateNullOutput(builder);
			}
			else if (declaration instanceof ClosureDeclaration)
			{
				ClosureDeclaration closure = (ClosureDeclaration)declaration;
				
				return closure.getTarget().generateSourceName(builder, "ref");
			}
		}
		
		return super.generateArgumentReference(builder, callingMethod);
	}
	
	public StringBuilder generateHeaderNativeInterface(StringBuilder builder)
	{
		MethodDeclaration[] methods = node().getVisibleNativeMethods();
		
            /*if (methods.length <= 0)
            {
                return builder;
            }*/
		
		String name = generateSourceName("native").toString();
		
		for (MethodDeclaration method : methods)
		{
			builder.append("typedef " + method.getTarget().generateType() + " (*");
			
			method.getTarget().generateSourceNativeName(builder, true).append(")(");
			
			ParameterList params = method.getParameterList();
			
			params.getTarget().generateHeader(builder).append(");\n");
		}
		
		builder.append("\ntypedef struct " + name + "\n");
		builder.append("{\n");
		
		for (MethodDeclaration method : methods)
		{
			method.getTarget().generateSourceNativeName(builder, true).append(" ");
			method.getTarget().generateSourceNativeName(builder, false).append(";\n");
		}
		
		builder.append("} " + name + ";\n");
		
		return builder;
	}
	
	public StringBuilder generateSourceNativeInterface(StringBuilder builder)
	{
		//		String name = generateSourceName("native").toString();
		
		MethodDeclaration[] methods = node().getVisibleNativeMethods();
		
		//		builder.append('\n');
		
		//		builder.append("struct " + name + "\n");
		builder.append("{\n");
		
		for (MethodDeclaration method : methods)
		{
			String value = "&" + method.getTarget().generateSourceName();
			
			if (method instanceof NovaMethodDeclaration)
			{
				NovaMethodDeclaration n = (NovaMethodDeclaration)method;
				
				if (n.isOverridden() && !(n instanceof Constructor))
				{
					value = "0";//getVTableNode().getName() + "." + n.generateVirtualMethodName();
				}
			}
			
			builder.append(value + ",\n");
		}
		
		builder.append("},\n");
		
		return builder;
	}
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		VTableList vtables = node().getVTableNodes();
		
		vtables.getTarget().generateHeader(builder).append('\n');
		
		if (node().containsNonStaticData() || node().containsVirtualMethods())
		{
			builder.append("CCLASS_CLASS").append('\n').append('(').append('\n');
			
			generateSourceName(builder).append(", ").append('\n').append('\n');
			
			VTable extension = node().getVTableNodes().getExtensionVTable();
			
			builder.append(extension.getTarget().generateType()).append("* ").append(VTable.IDENTIFIER).append(";\n");
			
			FieldList fields = node().getFieldList();
			
			fields.getTarget().generateNonStaticHeader(builder);
			
			if (node().containsNonStaticPrivateData())
			{
				builder.append("struct Private* prv;").append('\n');
			}
			
			builder.append(')').append('\n');
		}
		
		FieldList fields = node().getFieldList();
		
		fields.getTarget().generateStaticHeader(builder).append('\n');
		
		if (node().getStaticBlockList().getNumVisibleChildren() > 0)
		{
			StaticBlock child = node().getStaticBlockList().getChild(0);
			
			child.getTarget().generateHeader(builder, node());
		}
		
		MethodList constructors = node().getConstructorList();
		constructors.getTarget().generateHeader(builder);
		
		node().getDestructorList().getTarget().generateHeader(builder);
		node().getMethodList().getTarget().generateHeader(builder);
		node().getPropertyMethodList().getTarget().generateHeader(builder);
		node().getHiddenMethodList().getTarget().generateHeader(builder);
		node().getVirtualMethodList().getTarget().generateHeader(builder);
		
		return builder;
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		VTableList vtables = node().getVTableNodes();
		
		vtables.getTarget().generateSource(builder).append('\n');
		
		if (node().containsNonStaticPrivateData())
		{
			builder.append("CCLASS_PRIVATE").append('\n').append('(').append('\n').append(generatePrivateFieldsSource()).append(')').append('\n');
		}
		
		builder.append(generatePrivateMethodPrototypes());
		
		FieldList fields = node().getFieldList();
		
		fields.getTarget().generateStaticSource(builder);
		
		for (int i = node().getNumDefaultChildren(); i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			builder.append('\n').append(child.getTarget().generateSource());
		}
		
		fields = node().getFieldList();
		
		fields.getTarget().generateNonStaticSource(builder);
		
		generateStaticBlocksSource(builder);
		
		node().getConstructorList().getTarget().generateSource(builder);
		node().getDestructorList().getTarget().generateSource(builder);
		node().getMethodList().getTarget().generateSource(builder);
		node().getPropertyMethodList().getTarget().generateSource(builder);
		node().getHiddenMethodList().getTarget().generateSource(builder);
		node().getVirtualMethodList().getTarget().generateSource(builder);
		
		return builder;
	}
	
	private StringBuilder generateStaticBlocksSource(StringBuilder builder)
	{
		if (node().getStaticBlockList().getNumVisibleChildren() > 0)
		{
			StaticBlock block = node().getStaticBlockList().getChild(0);
			
			block.getTarget().generateMethodHeader(builder, node()).append('\n');
			
			builder.append('{').append('\n');
			
			for (int i = 0; i < node().getStaticBlockList().getNumVisibleChildren(); i++)
			{
				block = node().getStaticBlockList().getChild(i);
				
				block.getTarget().generateSource(builder);
			}
			
			builder.append('}').append('\n');
		}
		
		return builder;
	}
	
	/**
	 * Generate the C source representation of the private field
	 * declarations.
	 *
	 * @return The StringBuilder with the appended data.
	 */
	private StringBuilder generatePrivateFieldsSource()
	{
		return generatePrivateFieldsSource(new StringBuilder());
	}
	
	/**
	 * Generate the C source representation of the private field
	 * declarations.
	 *
	 * @param builder The StringBuilder to append that data to.
	 * @return The StringBuilder with the appended data.
	 */
	private StringBuilder generatePrivateFieldsSource(StringBuilder builder)
	{
		if (node().getExtendedClassDeclaration() != null)
		{
			ClassDeclaration clazz = node().getExtendedClassDeclaration();
			
			clazz.getTarget().generatePrivateFieldsSource(builder);
		}
		
		InstanceFieldList fields = node().getFieldList().getPrivateFieldList();
		
		return fields.getTarget().generateSource(builder);
	}
	
	public StringBuilder generateSourceName(StringBuilder builder, String uniquePrefix)
	{
		if (uniquePrefix == null)
		{
			uniquePrefix = Nova.LANGUAGE_NAME;
		}
		
		return generateUniquePrefix(builder).append(uniquePrefix).append("_").append(node().getName());
	}
	
	/**
	 * Generate the prototypes for specifically the private methods.
	 *
	 * @return A String containing the prototype definitions.
	 */
	private String generatePrivateMethodPrototypes()
	{
		StringBuilder  builder = new StringBuilder();
		
		generatePrototypes(builder, node().getMethodList());
		generatePrototypes(builder, node().getPropertyMethodList());
		
		if (builder.length() > 0)
		{
			builder.insert(0, '\n');
		}
		
		return builder.toString();
	}
	
	private void generatePrototypes(StringBuilder builder, MethodList methods)
	{
		for (int i = 0; i < methods.getNumChildren(); i++)
		{
			MethodDeclaration methodDeclaration = methods.getChild(i);
			
			if (methodDeclaration.getVisibility() == InstanceDeclaration.PRIVATE)
			{
				methodDeclaration.getTarget().generateSourcePrototype(builder).append('\n');
			}
		}
	}
	
	public StringBuilder generateUniquePrefix()
	{
		return generateUniquePrefix(new StringBuilder());
	}
	
	/**
	 * Get the prefix that is used for the data that is contained
	 * within the specified class.<br>
	 * <br>
	 * For example:
	 * <blockquote><pre>
	 * package "node()/is/my/package"
	 *
	 * public class Test
	 * {
	 * 	...
	 * }</pre></blockquote>
	 * The method prefix would look like:
	 * "<code>node()_is_my_package_NovaTest</code>"
	 *
	 * @return The prefix that is used for the data contained within
	 * 		the class.
	 */
	public StringBuilder generateUniquePrefix(StringBuilder builder)
	{
		Package p = node().getFileDeclaration().getPackage();
		
		return p.getTarget().generateLocation(builder).append('_');
	}
}
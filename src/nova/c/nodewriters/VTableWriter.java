package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class VTableWriter extends IIdentifierWriter
{
	public abstract VTable node();
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		NovaMethodDeclaration methods[] = node().getVirtualMethods();
		
		builder.append("typedef struct ").append(generateTypeName()).append(' ').append(generateTypeName()).append(";\n");
		
		if (methods.length <= 0)
		{
			return builder;
		}
		
		builder.append("struct ").append(generateTypeName()).append("\n{\n");
		
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			child.getTarget().generateHeaderFragment(builder).append(";\n");
		}
		
		generateVirtualMethodDeclarations(builder, methods);
		
		builder.append("}").append(";\n\n");
		
		return builder;
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		NovaMethodDeclaration methods[] = node().getVirtualMethods();
		
		if (methods.length <= 0)
		{
			return builder;
		}
		
		generateType(builder).append(' ').append(generateSourceName()).append(" =\n");
		
		builder.append("{\n");
		
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			child.getTarget().generateSourceFragment(builder).append(",\n");
		}
		
		generateVirtualMethodValues(builder, methods);
		
		builder.append("};\n");
		
		return builder;
	}
	
	/**
	 * Generate the virtual method declarations that declares the names
	 * of the methods that are used in the class and its ancestors.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @param methods The methods to add the identifiers from.
	 * @return The StringBuilder with the appended data.
	 */
	public StringBuilder generateVirtualMethodDeclarations(StringBuilder builder, NovaMethodDeclaration methods[])
	{
		for (NovaMethodDeclaration method : methods)
		{
			generateVirtualMethodDeclaration(builder, method);
		}
		
		return builder;
	}
	
	/**
	 * Generate the virtual method declaration that declares the name
	 * of the given method.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @param method The method to add the identifier from.
	 * @return The StringBuilder with the appended data.
	 */
	public StringBuilder generateVirtualMethodDeclaration(StringBuilder builder, NovaMethodDeclaration method)
	{
		VirtualMethodDeclaration virtual = method.getVirtualMethod();
		ParameterList params = method.getParameterList();
		
		return method.getTarget().generateType(builder).append(" (*").append(virtual.getTarget().generateVirtualMethodName()).append(")(").append(params.getTarget().generateHeader()).append(");\n");
	}
	
	/**
	 * Add the vtable values that point to the correct virtual method
	 * implementation for the specified class.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @param methods The methods to add the references to.
	 * @return The StringBuilder with the appended data.
	 */
	public StringBuilder generateVirtualMethodValues(StringBuilder builder, NovaMethodDeclaration methods[])
	{
		for (NovaMethodDeclaration method : methods)
		{
			if (method != null)
			{
				//				method.generateVirtualMethodName(builder);
				if (method instanceof AbstractMethodDeclaration)
				{
					method = method.getVirtualMethod();
				}
				
				method.getTarget().generateSourceName(builder);
			}
			else
			{
				builder.append(0);
			}
			
			builder.append(",\n");
		}
		
		return builder;
	}
}
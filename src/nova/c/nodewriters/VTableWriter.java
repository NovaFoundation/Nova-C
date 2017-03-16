package nova.c.nodewriters;

import net.fathomsoft.nova.Nova;
import net.fathomsoft.nova.tree.*;

public abstract class VTableWriter extends IIdentifierWriter
{
	public abstract VTable node();

	public StringBuilder generateTypedef(StringBuilder builder)
	{
		builder.append("typedef struct ").append(generateTypeName()).append(' ').append(generateTypeName()).append(";");

		return builder;
	}
	
	@Override
	public StringBuilder generateType(StringBuilder builder, boolean checkArray, boolean checkValueReference, boolean checkAllocatedOnHeap)
	{
		return generateTypeName(builder).append("");
	}
	
	@Override
	public StringBuilder generateTypeName(StringBuilder builder)
	{
		return generateTypeName(builder, false);
	}
	
	public StringBuilder generateTypeName(boolean full)
	{
		return generateTypeName(new StringBuilder(), full);
	}
	
	public StringBuilder generateTypeName(StringBuilder builder, boolean full)
	{
		return getWriter(node().getParentClass()).generateSourceName(builder).append(full ? "_full" : "").append("_VTable");
	}
	
	@Override
	public StringBuilder generateSourceName(StringBuilder builder, String uniquePrefix)
	{
		return generateTypeName(builder).append("_val");
	}
	
	public final StringBuilder generateHeader(StringBuilder builder)
	{
		return generateHeader(builder, false);
	}
	
	public StringBuilder generateHeader(StringBuilder builder, boolean full)
	{
		builder.append("struct ").append(generateTypeName(full)).append("\n{\n");
		
		writeChildrenHeader(builder);
		
		if (full)
		{
			NovaMethodDeclaration methods[] = node().getVirtualMethods();
			
			generateVirtualMethodDeclarations(builder, methods);
		}
		
		builder.append("}").append(";");
		
		return builder;
	}
	
	public StringBuilder writeChildrenHeader(StringBuilder builder)
	{
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			getWriter(child).generateHeaderFragment(builder).append(";\n");
		}
		
		return builder;
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		NovaMethodDeclaration methods[] = node().getVirtualMethods();
		
		generateType(builder).append(' ').append(generateSourceName()).append(" =\n{\n");
		
		writeChildrenSource(builder);
		
		generateVirtualMethodValues(builder, methods);
		
		builder.append("};\n");
		
		return builder;
	}
	
	public StringBuilder writeChildrenSource(StringBuilder builder)
	{
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			getWriter(child).generateSourceFragment(builder).append(",\n");
		}
		
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
		
		return getWriter(virtual).generateSource(builder);//getWriter(method).generateType(builder).append(" (*").append(getWriter(virtual).generateVirtualMethodName(method)).append(")(").append(getWriter(params).generateHeader()).append(");\n");
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
				
				getWriter(method).generateSourceName(builder);
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
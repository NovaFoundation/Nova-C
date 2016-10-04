package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class NovaMethodDeclarationWriter extends MethodDeclarationWriter
{
	public abstract NovaMethodDeclaration node();
	
	public StringBuilder generateInterfaceVTableSource(StringBuilder builder)
	{
		NovaMethodDeclaration root = node().getVirtualMethod();//.getRootDeclaration();
		NovaParameterList params = root.getParameterList();
		
		builder.append("(").append(root.getTarget().generateType()).append("(*)(").append(params.getTarget().generateHeader()).append("))");
		
		return generateSourceName(builder);
	}
	
	public StringBuilder generateClosureContext(StringBuilder builder)
	{
		return builder.append(NovaMethodDeclaration.NULL_IDENTIFIER);
	}
	
	public StringBuilder generateSourceNativeName(StringBuilder builder, boolean declaration)
	{
		super.generateSourceNativeName(builder, declaration);
		
		if (!declaration && node().isOverloaded())
		{
			for (Parameter param : node().getParameterList())
			{
				builder.append('_');
				
				String location = null;
				
				if (param.isExternalType())
				{
					location = param.getType();
				}
				else
				{
					ClassDeclaration clazz = param.getTypeClass();
					
					if (clazz != null)
					{
						location = clazz.getFileDeclaration().getPackage().getLocation().replace('/', '_');
						
						if (location.length() > 0)
						{
							location += '_';
						}
						
						location += clazz.getName();
					}
					else
					{
						location = "void";
					}
				}
				
				builder.append('_');
				
				if (param.isPrimitiveArray())
				{
					builder.append("Array" + param.getArrayDimensions() + "d_");
				}
				
				builder.append(location);
			}
		}
		
		return builder;
	}
	
	public StringBuilder generateInterfaceVTableHeader(StringBuilder builder)
	{
		VirtualMethodDeclaration virtual = node().getVirtualMethod();
		NovaParameterList params = node().getParameterList();
		
		return generateType(builder).append(" (*").append(virtual.getTarget().generateVirtualMethodName()).append(")(").append(params.getTarget().generateHeader()).append(");\n");
	}
	
	/**
	 * Generate the identifier that will be used to call the method.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The updated StringBuilder.
	 */
	public StringBuilder generateMethodCall(StringBuilder builder)
	{
		if (node().isVirtual())
		{
			VirtualMethodDeclaration virtual = node().getVirtualMethod();
			
			return virtual.getTarget().generateVirtualMethodName(builder);
		}
		
		return super.generateMethodCall(builder);
	}
	
	public StringBuilder generateSourceName(StringBuilder builder, String uniquePrefix)
	{
		return generateSourceName(builder, uniquePrefix, true);
	}
	
	public StringBuilder generateSourceName(StringBuilder builder, String uniquePrefix, boolean outputOverload)
	{
		if (node().overloadID == -1)
		{
			return super.generateSourceName(builder, uniquePrefix);
		}
		
		if (uniquePrefix == null)
		{
			uniquePrefix = "";
		}
		if (outputOverload)
		{
			uniquePrefix += node().overloadID;
		}
		
		return super.generateSourceName(builder, uniquePrefix);
	}
}
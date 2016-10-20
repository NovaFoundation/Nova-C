package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ParameterWriter extends LocalDeclarationWriter
{
	public abstract Parameter node();
	
	public StringBuilder generateTypeName(StringBuilder builder)
	{
		// Upsize the type in case a pointer is passed
		if (node().isOptional() && (node().getTypeClass().isOfType("nova/primitive/number/Char") || node().getTypeClass().isOfType("nova/primitive/Bool")))
		{
			return builder.append("int");
		}
		
		if (node().isObjectReference() && node().getType() != null)
		{
			return generateTypeClassName(builder);
		}
		/*else if (getTypeClass() != null && getTypeClass().equals(getProgram().getClassDeclaration(Nova.getClassLocation("Number"))))
		{
			return builder.append("long_long");
		}*/
		
		return super.generateTypeName(builder);
	}
	
	@Override
	public StringBuilder generateSourceName(StringBuilder builder, String uniquePrefix)
	{
		if (this instanceof ClosureDeclarationWriter)
		{
			return super.generateSourceName(builder, uniquePrefix);
		}
		
		if (uniquePrefix != null)
		{
			builder.append(uniquePrefix);
		}
		
		return builder.append(node().getName());
	}
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return generateModifiersSource(builder);
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateHeader(builder).append(' ').append(generateSourceName());
	}
}
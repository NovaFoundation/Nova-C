package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ParameterWriter extends LocalDeclarationWriter
{
	public abstract Parameter node();
	
	public StringBuilder generateTypeName(StringBuilder builder)
	{
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
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return generateModifiersSource(builder);
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateHeader(builder).append(' ').append(generateSourceName());
	}
}
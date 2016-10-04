package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ImportWriter extends NodeWriter
{
	public abstract Import node();
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return generateSource(builder);
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		builder.append("#include ");
		
		if (node().isExternal() || !node().getFileDeclaration().getName().equals(node().location))
		{
			return builder.append('<').append(node().getLocation()).append('>').append('\n');
		}
		else
		{
			return builder.append('"').append(node().getLocation()).append('"').append('\n');
		}
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class InterfaceWriter extends ClassDeclarationWriter
{
	public abstract Interface node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return super.generateSource(builder);
	}
}
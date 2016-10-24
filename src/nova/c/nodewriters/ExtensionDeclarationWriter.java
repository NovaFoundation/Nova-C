package nova.c.nodewriters;

import net.fathomsoft.nova.tree.ExtensionDeclaration;

public abstract class ExtensionDeclarationWriter extends ClassDeclarationWriter
{
	public abstract ExtensionDeclaration node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return super.generateSource(builder);
	}
}
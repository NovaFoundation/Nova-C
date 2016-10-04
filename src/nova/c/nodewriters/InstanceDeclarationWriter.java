package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class InstanceDeclarationWriter extends VariableDeclarationWriter
{
	public abstract InstanceDeclaration node();
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return generateHeaderFragment(builder).append(";\n");
	}
	
	public StringBuilder generateHeaderFragment(StringBuilder builder)
	{
		return generateModifiersSource(builder).append(' ').append(node().getName());
	}
}
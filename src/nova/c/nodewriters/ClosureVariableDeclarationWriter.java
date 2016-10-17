package nova.c.nodewriters;

import net.fathomsoft.nova.tree.ClosureVariableDeclaration;

public abstract class ClosureVariableDeclarationWriter extends VariableDeclarationWriter
{
	public abstract ClosureVariableDeclaration node();
	
	@Override
	public StringBuilder generateTypeName(StringBuilder builder)
	{
		return getWriter(node().originalDeclaration).generateTypeName(builder);
	}
}
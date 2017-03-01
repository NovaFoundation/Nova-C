package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.variables.VariableDeclaration;

public abstract class ChainedMethodCallWriter extends MethodCallWriter
{
	public abstract MethodCall node();
	
	@Override
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		return super.generateSourceFragment(builder);
	}
}
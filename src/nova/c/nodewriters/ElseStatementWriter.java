package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ElseStatementWriter extends ControlStatementWriter
{
	public abstract ElseStatement node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		builder.append("else");
		
		if (node().getNumChildren() == 2)
		{
			Node child = node().getChild(1);
			
			if (child instanceof IfStatement)
			{
				builder.append(' ');
				
				child.getTarget().generateSourceFragment(builder);
			}
		}
		
		builder.append('\n');
		
		Scope scope = node().getScope();
		
		return scope.getTarget().generateSource(builder);
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class WhileLoopWriter extends LoopWriter
{
	public abstract WhileLoop node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		Node condition = node().getCondition();
		
		builder.append("while (").append(condition.getTarget().generateSourceFragment()).append(')').append('\n');
		
		Scope scope = node().getScope();
		
		scope.getTarget().generateSource(builder);
		
		return builder;
	}
}
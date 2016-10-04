package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ForLoopWriter extends LoopWriter
{
	public abstract ForLoop node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		Assignment initialization = node().getLoopInitialization();
		Node       condition      = node().getCondition();
		Node       update         = node().getLoopUpdate();
		
		if (initialization != null)
		{
			initialization.getTarget().generateSource(builder);//.append('\n');
		}
		
		builder.append("for (; ");
		
		if (condition != null)
		{
			condition.getTarget().generateSourceFragment(builder);
		}
		
		builder.append("; ");
		
		if (update != null)
		{
			update.getTarget().generateSourceFragment(builder);
		}
		
		builder.append(')').append('\n');
		
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			if (child != node().getArgumentList())
			{
				child.getTarget().generateSource(builder);
			}
		}
		
		return builder;
	}
}
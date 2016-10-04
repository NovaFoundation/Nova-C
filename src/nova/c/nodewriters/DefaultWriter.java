package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.match.Default;

public abstract class DefaultWriter extends MatchCaseWriter
{
	public abstract Default node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		Scope scope = node().getScope();
		
		if (node().getParentSwitch().isConventionalSwitch())
		{
			builder.append("default:").append('\n');
			
			scope.getTarget().generateSource(builder, false);
		}
		else
		{
			builder.append("else").append('\n');
			
			scope.getTarget().generateSource(builder);
		}
		
		return builder;
	}
}
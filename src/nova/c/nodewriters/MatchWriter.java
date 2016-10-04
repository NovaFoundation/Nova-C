package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.match.Match;

public abstract class MatchWriter extends ControlStatementWriter
{
	public abstract Match node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		Scope scope = node().getScope();
		
		if (node().isConventionalSwitch())
		{
			Value control = node().getControlValue();
			
			builder.append("switch (" + control.getTarget().generateSourceFragment() + ")\n");
			
			scope.getTarget().generateSource(builder);
		}
		else
		{
			boolean requiresFacade = node().requiresLoopFacade();
			
			if (requiresFacade)
			{
				builder.append("do\n{\n");
			}
			
			scope.getTarget().generateSource(builder, false);
			
			if (requiresFacade)
			{
				builder.append("}\nwhile (0);\n");
			}
		}
		
		return builder;
	}
}
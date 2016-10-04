package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.match.Case;
import net.fathomsoft.nova.tree.variables.Variable;

public abstract class CaseWriter extends MatchCaseWriter
{
	public abstract Case node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		Scope scope = node().getScope();
		
		if (node().getParentSwitch().isConventionalSwitch())
		{
			Value value = node().getValue();
			
			builder.append("case " + value.getTarget().generateSourceFragment() + ":\n");
			
			scope.getTarget().generateSource(builder, false);
			
			if (node().requiresBreak())
			{
				builder.append("break;\n");
			}
		}
		else
		{
			Value controlValue = node().getParentSwitch().getControlValue();
			
			String control = controlValue.getTarget().generateSourceFragment().toString();
			
			Case before = null;
			String fall   = "";
			
			if (node().getParent().getChildBefore(node()) instanceof Case)
			{
				before = (Case)node().getParent().getChildBefore(node());
			}
			
			if (before != null)
			{
				if (before.containsFallthrough())
				{
					Variable fallthrough = node().getParentSwitch().getLocalFallthrough();
					
					fall = fallthrough.getTarget().generateSourceFragment() + " || ";
				}
				else
				{
					builder.append("else ");
				}
			}
			
			Value value = node().getValue();
			
			builder.append("if (" + fall + control + " == " + value.getTarget().generateSourceFragment() + ")").append('\n');
			builder.append("{\n");
			
			scope.getTarget().generateSource(builder, false);
			
			if (node().getParentSwitch().requiresLoopFacade() && node().requiresBreak())
			{
				builder.append("break;\n");
			}
			
			builder.append("}\n");
		}
		
		return builder;
	}
}
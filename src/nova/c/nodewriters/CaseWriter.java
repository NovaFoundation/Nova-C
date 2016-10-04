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
			
			builder.append("case " + getWriter(value).generateSourceFragment() + ":\n");
			
			getWriter(scope).generateSource(builder, false);
			
			if (node().requiresBreak())
			{
				builder.append("break;\n");
			}
		}
		else
		{
			Value controlValue = node().getParentSwitch().getControlValue();
			
			String control = getWriter(controlValue).generateSourceFragment().toString();
			
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
					
					fall = getWriter(fallthrough).generateSourceFragment() + " || ";
				}
				else
				{
					builder.append("else ");
				}
			}
			
			Value value = node().getValue();
			
			builder.append("if (" + fall + control + " == " + getWriter(value).generateSourceFragment() + ")").append('\n');
			builder.append("{\n");
			
			getWriter(scope).generateSource(builder, false);
			
			if (node().getParentSwitch().requiresLoopFacade() && node().requiresBreak())
			{
				builder.append("break;\n");
			}
			
			builder.append("}\n");
		}
		
		return builder;
	}
}
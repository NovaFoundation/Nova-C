package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class TernaryOperationWriter extends IValueWriter implements AccessibleWriter
{
	public abstract TernaryOperation node();
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		Value t = node().getTrueValue();
		Value f = node().getFalseValue();
		
		String trueValue = getWriter(t).generateSourceFragment().toString();
		String falseValue = getWriter(f).generateSourceFragment().toString();
		
		ClassDeclaration trueType = t.getReturnedNode().getTypeClass();
		ClassDeclaration falseType = f.getReturnedNode().getTypeClass();
		
		if (trueType != falseType)
		{
			ClassDeclaration commonClass = trueType.getCommonAncestor(f.getReturnedNode().getTypeClass());
			
			if (trueType != commonClass)
			{
				Value r = f.getReturnedNode();
				
				trueValue = getWriter(r).generateTypeCast() + trueValue;
			}
			else
			{
				Value r = t.getReturnedNode();
				
				falseValue = getWriter(r).generateTypeCast() + falseValue;
			}
		}
		
		Value condition = node().getCondition();
		
		return generateTypeCast(builder).append('(').append(getWriter(condition).generateSourceFragment()).append(" ? ").append(trueValue).append(" : ").append(falseValue).append(')');
	}
}
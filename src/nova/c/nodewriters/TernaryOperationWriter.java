package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class TernaryOperationWriter extends IValueWriter implements AccessibleWriter
{
	public abstract TernaryOperation node();
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		Value t = node().getTrueValue();
		Value f = node().getFalseValue();
		
		String trueValue = t.getTarget().generateSourceFragment().toString();
		String falseValue = f.getTarget().generateSourceFragment().toString();
		
		ClassDeclaration trueType = t.getReturnedNode().getTypeClass();
		ClassDeclaration falseType = f.getReturnedNode().getTypeClass();
		
		if (trueType != falseType)
		{
			ClassDeclaration commonClass = trueType.getCommonAncestor(f.getReturnedNode().getTypeClass());
			
			if (trueType != commonClass)
			{
				Value r = f.getReturnedNode();
				
				trueValue = r.getTarget().generateTypeCast() + trueValue;
			}
			else
			{
				Value r = t.getReturnedNode();
				
				falseValue = r.getTarget().generateTypeCast() + falseValue;
			}
		}
		
		Value condition = node().getCondition();
		
		return condition.getTarget().generateSourceFragment(builder).append(" ? ").append(trueValue).append(" : ").append(falseValue);
	}
}
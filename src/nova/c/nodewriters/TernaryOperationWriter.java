package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.generics.GenericTypeArgument;

public abstract class TernaryOperationWriter extends IValueWriter implements AccessibleWriter
{
	public abstract TernaryOperation node();
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		Value t = node().getTrueValue();
		Value f = node().getFalseValue();
		
		String trueValue = getWriter(t).generateSourceFragment().toString();
		String falseValue = getWriter(f).generateSourceFragment().toString();
		
		Value trueNode = t.getReturnedNode();
		Value falseNode = f.getReturnedNode();
		
		ClassDeclaration trueType = null;
		ClassDeclaration falseType = null;
		
		if (trueNode instanceof MethodCall)
		{
			trueNode = (Value)((MethodCall)trueNode).getCallableMethodBase();
		}
		if (falseNode instanceof MethodCall)
		{
			falseNode = (Value)((MethodCall)falseNode).getCallableMethodBase();
		}
		
		trueType = trueNode.getTypeClass();
		falseType = falseNode.getTypeClass();
		
		if (trueType != falseType)
		{
			ClassDeclaration commonClass = trueType.getCommonAncestor(falseNode.getTypeClass());
			Value castNode;
			
			if (trueType != commonClass)
			{
				castNode = falseNode;
			}
			else
			{
				castNode = trueNode;
			}
			
			trueValue = getWriter(castNode).generateTypeCast() + trueValue;
			falseValue = getWriter(castNode).generateTypeCast() + falseValue;
		}
		
//		if (trueNode.isGenericType())
//		{
//			GenericTypeArgument arg = trueNode.getGenericTypeParameter().getCorrespondingArgument(trueNode);
//			
//			if (arg != null && !arg.isGenericType())
//			{
//				trueValue = getWriter(arg).generateTypeCast() + trueValue;
//			}
//		}
//		if (falseNode.isGenericType())
//		{
//			GenericTypeArgument arg = falseNode.getGenericTypeParameter().getCorrespondingArgument(falseNode);
//			
//			if (arg != null && !arg.isGenericType())
//			{
//				falseValue = getWriter(arg).generateTypeCast() + falseValue;
//			}
//		}
		
		Value condition = node().getCondition();
		
		return generateTypeCast(builder).append('(').append(getWriter(condition).generateSourceFragment()).append(" ? ").append(trueValue).append(" : ").append(falseValue).append(')');
	}
}
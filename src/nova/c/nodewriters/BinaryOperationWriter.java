package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class BinaryOperationWriter extends IValueWriter
{
	public abstract BinaryOperation node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		generateSourceFragment(builder);
		
		if (node().getOperator().isShorthand())
		{
			builder.append(";\n");
		}
		
		return builder;
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		Operator operator = node().getOperator();
		
		if (node().getNumChildren() == 1)
		{
			Value operand = node().getLeftOperand();
			
			return getWriter(operand).generateSourceFragment(builder);
		}
		
		String leftCast = "";
		String rightCast = "";
		
		Value left = node().getLeftOperand();
		Value right = node().getRightOperand();
		Value leftReturned = left.getReturnedNode();
		Value rightReturned = right.getReturnedNode();
		
		if (leftReturned.isOriginallyGenericType())
		{
			leftCast = getWriter(leftReturned).generateTypeCast(new StringBuilder(), true, false).toString();
		}
		if (rightReturned.isOriginallyGenericType())
		{
			rightCast = getWriter(rightReturned).generateTypeCast(new StringBuilder(), true, false).toString();
		}
		
		if (operator.operator.equals(Operator.UR_SHIFT))
		{
			leftCast = "(unsigned int)" + leftCast;
		}
		
		return builder.append(leftCast).append(getWriter(left).generateSourceFragment()).append(' ')
			.append(getWriter(operator).generateSourceFragment()).append(' ')
			.append(rightCast).append(getWriter(right).generateSourceFragment());
	}
}
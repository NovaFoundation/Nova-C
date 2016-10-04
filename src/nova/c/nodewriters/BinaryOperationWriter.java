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
			
			return operand.getTarget().generateSourceFragment(builder);
		}
		
		String leftCast = "";
		String rightCast = "";
		
		Value left = node().getLeftOperand();
		Value right = node().getRightOperand();
		Value leftReturned = left.getReturnedNode();
		Value rightReturned = right.getReturnedNode();
		
		if (leftReturned.isOriginallyGenericType())
		{
			leftCast = leftReturned.getTarget().generateTypeCast(new StringBuilder(), true, false).toString();
		}
		if (rightReturned.isOriginallyGenericType())
		{
			rightCast = rightReturned.getTarget().generateTypeCast(new StringBuilder(), true, false).toString();
		}
		
		return builder.append(leftCast).append(left.getTarget().generateSourceFragment()).append(' ')
			.append(operator.getTarget().generateSourceFragment()).append(' ')
			.append(rightCast).append(right.getTarget().generateSourceFragment());
	}
}
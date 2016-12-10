package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class AssignmentWriter extends ValueWriter
{
	public abstract Assignment node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateSourceFragment(builder).append(";\n");
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		if (node().getAssignedNodeValue().getDataType() == Value.POINTER &&
			node().getAssignmentNode().getReturnedNode().getDataType() == Value.VALUE ||
			node().getAssignedNodeValue().getDataType() == Value.DOUBLE_POINTER &&
				node().getAssignmentNode().getReturnedNode().getDataType() == Value.POINTER)
		{
			builder.append('*');
		}
		
		Value assignee = node().getAssigneeNode();
		
		return getWriter(assignee).generateSourceFragment(builder).append(" = ").append(generateAssignmentSource());
	}
	
	/**
	 * Generate the assignment's right hand value C output.
	 *
	 * @return The assignment's right hand value C output.
	 */
	private StringBuilder generateAssignmentSource()
	{
		return generateAssignmentSource(new StringBuilder());
	}
	
	/**
	 * Generate the assignment's right hand value C output.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The assignment's right hand value C output.
	 */
	private StringBuilder generateAssignmentSource(StringBuilder builder)
	{
		Value assignment = node().getAssignmentNode();
		
		String assignmentType = assignment.getReturnedNode().getType();
		String assignedType = node().getAssignedNodeValue().getType();
		
		boolean sameType = assignedType == null || assignmentType.equals(assignedType);
		
		if (sameType && assignment instanceof Accessible)
		{
			MethodCall call = (MethodCall)((Accessible)assignment).getLastAccessedOfType(MethodCall.class, false);
			
			if (call != null)
			{
				sameType = !call.isVirtual();
			}
		}
		
		if (!sameType)
		{
			Value assigned = node().getAssignedNodeValue();
			Value returned = assignment.getReturnedNode();
			
			getWriter(assigned).generateTypeCast(builder, true, false).append(getWriter(returned).generatePointerToValueConversion(returned)).append('(');
		}
		
		builder.append(assignment.generateDataTypeOutput(node().getAssignedNodeValue().getDataType())).append(getWriter(assignment).generateSourceFragment());
		
		if (!sameType)
		{
			builder.append(')');
		}
		
		return builder;
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class DefaultParameterInitializationWriter extends NodeWriter
{
	public abstract DefaultParameterInitialization node();
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		String use = node().parameter.getTarget().generateUseOutput().toString();
		
		builder.append(use).append(" = ");
		node().parameter.getTarget().generateTypeCast(builder).append('(');
		
		builder.append(use).append(" == ");
		
		if (node().parameter.isPrimitive())
		{
			builder.append("(intptr_t)nova_null");
		}
		else
		{
			builder.append(0);
		}
		
		ClassDeclaration obj = node().getProgram().getClassDeclaration("nova/Object");
		
		String cast = !node().parameter.getDefaultValue().isPrimitive() ? obj.getTarget().generateTypeCast().toString() : "";
		
		Value defaultValue = node().parameter.getDefaultValue();
		
		builder.append(" ? ").append(cast).append(defaultValue.getTarget().generateSourceFragment()).append(" : ").append(cast).append(use);
		
		return builder.append(");");
	}
}
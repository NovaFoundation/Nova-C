package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ClosureContextDeclarationWriter extends LocalDeclarationWriter
{
	public abstract ClosureContextDeclaration node();
	
	public StringBuilder generateDeclarationFragment(StringBuilder builder)
	{
		return builder.append(node().context.getName()).append("* ").append(node().getName());
	}
	
	public StringBuilder generateDefaultValue(StringBuilder builder)
	{
		builder.append("NOVA_MALLOC(sizeof(").append(node().context.getName()).append("))");
		
		for (ClosureVariableDeclaration var : node().context)
		{
			builder.append(";\n").append(node().getName()).append("->");
			builder.append(getWriter(var).generateSourceName()).append(" = ");
			generateDeclarationValue(builder, var);
		}
		
		return builder;
	}
	
	public StringBuilder generateDeclarationValue(StringBuilder builder, ClosureVariableDeclaration var)
	{
		//Variable v = var.generateUsableVariable(node(), Location.INVALID);
		
		if (var.originalDeclaration instanceof ClosureVariableDeclaration)
		{
			builder.append("context->");
		}
		else
		{
			builder.append('&');
		}
		
		return getWriter(var.originalDeclaration).generateSourceName(builder);
	}
}
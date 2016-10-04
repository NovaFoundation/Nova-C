package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ClosureContextDeclarationWriter extends LocalDeclarationWriter
{
	public abstract ClosureContextDeclaration node();
	
	public StringBuilder generateDeclarationFragment(StringBuilder builder)
	{
		return builder.append(node().context.getName()).append(' ').append(node().getName());
	}
	
	public StringBuilder generateDefaultValue(StringBuilder builder)
	{
		builder.append("\n{\n");
		
		for (ClosureVariableDeclaration var : node().context)
		{
			generateDeclarationValue(builder, var);
		}
		
		return builder.append("}");
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
		
		getWriter(var.originalDeclaration).generateSourceName(builder);
		
		return builder.append(",\n");
	}
}
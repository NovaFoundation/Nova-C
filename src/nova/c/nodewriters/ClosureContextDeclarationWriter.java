package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.variables.VariableDeclaration;

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
			builder.append(";\n");
			
			generateDeclaration(builder, var);
		}
		
		return builder;
	}
	
	public StringBuilder generateLeftAssignment(StringBuilder builder)
	{
		return builder.append(node().getName()).append("->");
	}
	
	public StringBuilder generateDeclaration(StringBuilder builder, ClosureVariableDeclaration var)
	{
		generateLeftAssignment(builder);
		builder.append(getWriter(var).generateSourceName()).append(" = ");
		generateDeclarationValue(builder, var);
		
		VariableDeclaration root = var.getRootDeclaration();
		
		if (root instanceof ClosureDeclaration)
		{
			String context = var.originalDeclaration != root ? "context->" : "";
			
			builder.append(";\n");
			generateLeftAssignment(builder);
			getWriter(root).generateObjectReferenceIdentifier(builder).append(" = ").append(context);
			getWriter(root).generateObjectReferenceIdentifier(builder).append(";\n");
			
			generateLeftAssignment(builder);
			builder.append(root.getContextName()).append(" = ").append(context);
			builder.append(root.getContextName());
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
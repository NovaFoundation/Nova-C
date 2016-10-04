package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.variables.VariableDeclaration;
import net.fathomsoft.nova.tree.variables.VariableDeclarationList;

public abstract class VariableDeclarationListWriter extends ListWriter
{
	public abstract VariableDeclarationList node();
	
	/**
	 * Generate the output needed to free the variables after they are
	 * finished with.
	 *
	 * @return The String output of the variables being freed.
	 */
	public StringBuilder generateFreeVariablesOutput(StringBuilder builder)
	{
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			VariableDeclaration variable = (VariableDeclaration)node().getChild(i);
			
			variable.getTarget().generateFreeOutput(builder);
		}
		
		return builder;
	}
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return builder;
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			LocalDeclaration child = (LocalDeclaration)node().getChild(i);
			
			child.getTarget().generateDeclarationFragment(builder).append(" = ");
			child.getTarget().generateDefaultValue(builder);
			
			builder.append(";\n");
		}
		
		if (node().getNumChildren() > 0)
		{
			builder.append('\n');
		}
		
		return builder;
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.exceptionhandling.Exception;
import net.fathomsoft.nova.tree.variables.VariableDeclaration;

public abstract class VariableDeclarationWriter extends IIdentifierWriter
{
	public abstract VariableDeclaration node();
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return generateSource(builder);
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateDeclarationFragment(builder).append(";\n");
	}
	
	/**
	 * Generate a String with the declaration modifiers and the name of
	 * the variable declared.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The appended StringBuilder.
	 */
	public StringBuilder generateDeclarationFragment(StringBuilder builder)
	{
		return generateModifiersSource(builder).append(' ').append(generateSourceName());
	}
	
	/**
	 * Generate the modifiers for the specified Variable.<br>
	 * <br>
	 * For example:
	 * <blockquote><pre>
	 * Person people[] = new Person[42];</pre></blockquote>
	 * In the above Variable declaration, the modifiers are the type of
	 * the variable ("<u><code>Person</code></u>") and the type of
	 * declaration is an array.<br>
	 * node() also checks if the type requires a pointer.
	 *
	 * @param builder The StringBuilder to append to.
	 * @return The appended StringBuilder.
	 */
	public StringBuilder generateModifiersSource(StringBuilder builder)
	{
		if (node().isVolatile())//!(node() instanceof Parameter || node() instanceof FieldDeclaration))
		{
			builder.append(node().getVolatileText()).append(' ');
		}
		
		generateType(builder);
		
		return builder;
	}
	
	public StringBuilder generateDefaultValue(StringBuilder builder)
	{
		if (node().isPrimitive())
		{
			builder.append(0);
		}
		else
		{
			builder.append(generateTypeCast()).append(Value.NULL_IDENTIFIER);
		}
		
		return builder;
	}
	
	/**
	 * Generate a String for the code used to free memory of the
	 * specified variable.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The generated String for the code.
	 */
	public StringBuilder generateFreeOutput(StringBuilder builder)
	{
		if (node().isConstant())
		{
			return builder;
		}
		
		if (node().isPrimitiveType() || node().isExternalType())
		{
			if (!node().isPrimitive())
			{
				builder.append("NOVA_FREE(");
				
				generateUseOutput(builder, true).append(");\n");
			}
		}
		else
		{
			Destructor destructor = node().getTypeClass().getDestructor();
			
			destructor.getTarget().generateSourceName(builder).append('(').append('&');
			
			generateUseOutput(builder, true).append(", ").append(Exception.EXCEPTION_DATA_IDENTIFIER).append(");\n");
		}
		
		return builder;
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.variables.Variable;

public abstract class ClosureDeclarationWriter extends ParameterWriter
{
	public abstract ClosureDeclaration node();
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return generateSource(builder);
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateSourceFragment(builder);
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		builder.append(generateType()).append(' ').append(generateSourceName()).append(", ");
		
		Parameter ref = node().getParameterList().getObjectReference();
		
		ref.getTarget().generateType(builder).append(' ');
		
		generateObjectReferenceIdentifier(builder).append(", ");
		generateContextParameter(builder);
		
		return builder;
	}
	
	public StringBuilder generateArguments(StringBuilder builder, Variable context, NovaMethodDeclaration method)
	{
		if (context.getRootReferenceNode() instanceof ClassDeclaration == false)
		{
			Accessible root = context.getRootReferenceNode();
			
			root.getTarget().generateArgumentReference(builder, context);
		}
		else
		{
			builder.append(ClosureDeclaration.NULL_IDENTIFIER);//method.getParameterList().getObjectReference().generateNullOutput(builder);
		}
		
		builder.append(", ");
		method.getTarget().generateClosureContext(builder);
		
		return builder;
	}
	
	public StringBuilder generateObjectReferenceIdentifier(StringBuilder builder)
	{
		return builder.append(generateSourceName("ref"));
	}
	
	public StringBuilder generateContextParameter()
	{
		return generateContextParameter(new StringBuilder());
	}
	
	public StringBuilder generateContextParameter(StringBuilder builder)
	{
		return builder.append("void* ").append(node().getContextName());
	}
	
	public StringBuilder generateType(StringBuilder builder, boolean checkArray, boolean checkValueReference)
	{
		return builder.append(generateSourceName("closure" + node().id));
	}
	
	/**
	 * Generate the C type definition for the closure of the specified
	 * method declaration.<br>
	 * <br>
	 * For example:
	 * <blockquote><pre>
	 * public void test()
	 * {
	 * 	...
	 * }</pre></blockquote>
	 * will output will have the effect of
	 * "<code>typedef void (*closure_test)();</code>"
	 *
	 * @return The C closure type definition for the method.
	 */
	public StringBuilder generateClosureDefinition(StringBuilder builder)
	{
		builder.append("typedef ");
		
		super.generateType(builder, true, true).append(" (*").append(generateSourceName("closure" + node().id)).append(')');
		
		ParameterList params = node().getParameterList();
		
		builder.append('(').append(params.getTarget().generateHeader()).append(')').append(";\n");
		
		return builder;
	}
}
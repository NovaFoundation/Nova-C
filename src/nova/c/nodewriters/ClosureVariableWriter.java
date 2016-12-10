package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ClosureVariableWriter extends NovaMethodDeclarationWriter
{
	public abstract ClosureVariable node();
	
	@Override
	public StringBuilder generateUseOutput(StringBuilder builder, boolean pointer, boolean checkAccesses)
	{
		return super.generateUseOutput(builder, pointer, checkAccesses);
	}
	
	public StringBuilder generateDeclaration(StringBuilder builder)
	{
		generateFunctionPointer(builder).append(";\n");
		builder.append("void* ").append(generateContextName()).append(";\n");
		builder.append("void* ").append(generateReferenceName()).append(";\n");
		
		return builder;
	}
	
	public StringBuilder generateContextName()
	{
		return generateContextName(new StringBuilder());
	}
	
	public StringBuilder generateContextName(StringBuilder builder)
	{
		return generateSourceName(builder, "context");
	}
	
	public StringBuilder generateReferenceName()
	{
		return generateReferenceName(new StringBuilder());
	}
	
	public StringBuilder generateReferenceName(StringBuilder builder)
	{
		return generateSourceName(builder, "reference");
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return builder;//generateSourceFragment(builder);
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		return generateSourceName(builder);
	}
}
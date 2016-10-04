package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ClosureContextWriter extends TypeListWriter
{
	public abstract ClosureContext node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateSourceFragment(builder).append(";\n");
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		builder.append("typedef struct\n");
		builder.append("{\n");
		
		for (ClosureVariableDeclaration var : node())
		{
			builder.append("/* ").append(var.originalDeclaration).append(" */ ");
			getWriter(var).generateSource(builder);
			
                /*boolean original = var.originalDeclaration.isValueReference();
                var.originalDeclaration.setIsValueReference(true);
                var.originalDeclaration.generateSource(builder);
                var.originalDeclaration.setIsValueReference(original);*/
		}
		
		builder.append("} ").append(node().getName());
		
		return builder;
	}
}
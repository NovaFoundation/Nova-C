package nova.c.nodewriters;

import net.fathomsoft.nova.Nova;
import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.variables.Variable;
import net.fathomsoft.nova.tree.variables.VariableDeclaration;

public abstract class ChainedMethodCallWriter extends MethodCallWriter
{
	public abstract ChainedMethodCall node();
	
	@Override
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		builder.append("(");
		
		if (node().chained != null)
		{
			getWriter(((FunctionType)node().getTypeObject()).closure).generateTypeCast(builder);
			
			builder.append("(");
			getWriter(node().variable).generateSourceName(builder).append(" = ");
			getWriter(node().variable).generateTypeCast(builder);
			
			getWriter(node().chained).generateSourceFragment(builder).append(")");
		}
		else
		{
			Identifier ref = node().getChainReference();
			
			getWriter(((FunctionType)ref.getTypeObject()).closure).generateTypeCast(builder);
			
			getWriter(ref).generateSourceName(builder);
		}
		
		builder.append("->func)");
		
		MethodCallArgumentList args = node().getArgumentList();
		
		getWriter(args).generateSource(builder);
		
		return builder;
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.exceptionhandling.Catch;

public abstract class CatchWriter extends ExceptionHandlerWriter
{
	public abstract Catch node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		builder.append("CATCH ").append('(').append(node().getException().getID()).append(')').append('\n');
		
		Scope scope = node().getScope();
		
		scope.getTarget().generateSource(builder);
		
		return builder;
	}
}
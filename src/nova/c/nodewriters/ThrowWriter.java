package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.exceptionhandling.Throw;

public abstract class ThrowWriter extends ExceptionHandlerWriter
{
	public abstract Throw node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		builder.append("THROW").append('(');
		Identifier exception = node().getExceptionInstance();
		
		getWriter(exception).generateSourceFragment(builder).append(')').append(';').append('\n');
		
		return builder;
	}
}
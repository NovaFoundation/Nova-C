package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.exceptionhandling.Exception;

public abstract class StaticBlockWriter extends NodeWriter
{
	public abstract StaticBlock node();
	
	public StringBuilder generateHeader(StringBuilder builder, ClassDeclaration clazz)
	{
		return generateMethodHeader(builder, clazz).append(';').append('\n');
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		Scope scope = node().getScope();
		
		return getWriter(scope).generateSource(builder);
	}
	
	public StringBuilder generateMethodHeader(StringBuilder builder, ClassDeclaration clazz)
	{
		builder.append("void ");
		
		generateMethodName(builder, clazz);
		
		ParameterList params = node().getParameterList();
		
		builder.append('(').append(getWriter(params).generateSource()).append(')');
		
		return builder;
	}
	
	public static StringBuilder generateMethodName(StringBuilder builder, ClassDeclaration clazz)
	{
		return builder.append(getWriter(clazz).generateSourceName()).append(StaticBlock.C_PREFIX).append(StaticBlock.IDENTIFIER);
	}
	
	public static StringBuilder generateMethodCall(StringBuilder builder, ClassDeclaration clazz)
	{
		return generateMethodName(builder, clazz).append("(" + Exception.EXCEPTION_DATA_IDENTIFIER + ")");
	}
}
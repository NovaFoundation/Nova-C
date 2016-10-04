package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.util.SyntaxUtils;

public abstract class ReturnWriter extends IValueWriter
{
	public abstract Return node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateSourceFragment(builder).append(";\n");
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		builder.append("return");
		
		Value value = node().getValueNode();
		
		if (value != null)
		{
			builder.append(' ');
			
			if (value.getReturnedNode().isGenericType(true) || !SyntaxUtils.isSameType(node().getParentMethod(), value.getReturnedNode(), false))
			{
				NovaMethodDeclaration method = node().getParentMethod();
				Value r = value.getReturnedNode();
				
				getWriter(method).generateTypeCast(builder).append(getWriter(r).generatePointerToValueConversion(r));
			}
			
			getWriter(value).generateSourceFragment(builder);
		}
		
		return builder;
	}
}
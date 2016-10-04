package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class CastWriter extends IValueWriter
{
	public abstract Cast node();
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		builder.append('(').append(generateType()).append(')');
		
		Value value = node().getValueNode();
		Value ret = value.getReturnedNode();
		
		ret.getTarget().generatePointerToValueConversion(builder);
		value.getTarget().generateSourceFragment(builder);
		
		return builder;
	}
}
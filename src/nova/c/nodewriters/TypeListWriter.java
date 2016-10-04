package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class TypeListWriter extends ListWriter
{
	public abstract TypeList node();
	
	public StringBuilder generateHeaderFragment(StringBuilder builder)
	{
		for (Object child : node())
		{
			((Node)child).getTarget().generateHeader(builder);
		}
		
		return builder;
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		for (Object child : node())
		{
			((Node)child).getTarget().generateSource(builder);
		}
		
		return builder;
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class StaticClassReferenceWriter extends IIdentifierWriter
{
	public abstract StaticClassReference node();
	
	public StringBuilder generateUseOutput(StringBuilder builder, boolean pointer, boolean checkAccesses)
	{
		return builder.append(0);
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		if (!node().doesAccess())
		{
			return generateUseOutput(builder);
		}
		
		if (node().isSpecialFragment())
		{
			return generateSpecialFragment(builder);
		}
		
		Identifier accessed = node().getAccessedNode();
		
		return accessed.getTarget().generateSourceFragment(builder);
	}
	
	public StringBuilder generateArgumentReference(StringBuilder builder, Identifier callingMethod)
	{
		return builder.append(0);//getAccessedNode().generateArgumentReference(builder, callingMethod);
	}
}
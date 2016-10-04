package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class InterfaceVTableWriter extends VTableWriter
{
	public abstract InterfaceVTable node();
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return builder;
	}
	
	public StringBuilder generateHeaderFragment(StringBuilder builder)
	{
		return generateType(builder).append(" ").append(InterfaceVTable.IDENTIFIER);
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return builder;
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		NovaMethodDeclaration[] methods = node().getVirtualMethods();
		
		builder.append("{\n");
		
		for (NovaMethodDeclaration method : methods)
		{
			if (method != null)
			{
				getWriter(method).generateInterfaceVTableSource(builder);
			}
			else
			{
				builder.append(0);
			}
			
			builder.append(",\n");
		}
		
		builder.append("}");
		
		return builder;
		//return super.generateSourceFragment(builder);
	}
}
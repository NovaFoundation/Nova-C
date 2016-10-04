package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.variables.VariableDeclaration;

public abstract class ClosureWriter extends VariableWriter
{
	public abstract Closure node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateSourceFragment(builder);
	}
	
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		ClosureDeclaration decl = node().getClosureDeclaration();
		
		getWriter(decl).generateTypeCast(builder);
		
		if (node().getMethodDeclaration().isVirtual() && !node().isVirtualTypeKnown())
		{
			Accessible root = node().getRootReferenceNode();
			
			getWriter(root).generateArgumentReference(builder, node()).append("->").append(VTable.IDENTIFIER).append("->");
			
			VirtualMethodDeclaration virtual = node().getMethodDeclaration().getVirtualMethod();
			
			builder.append(getWriter(virtual).generateVirtualMethodName());
		}
		else
		{
			VariableDeclaration d = node().getDeclaration();
			
			builder.append('&').append(getWriter(d).generateSourceName());
		}
		
		builder.append(", ");
		
		getWriter(decl).generateArguments(builder, node(), node().getMethodDeclaration());
		
		return builder;
	}
}
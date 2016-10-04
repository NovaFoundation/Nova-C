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
		
		decl.getTarget().generateTypeCast(builder);
		
		if (node().getMethodDeclaration().isVirtual() && !node().isVirtualTypeKnown())
		{
			Accessible root = node().getRootReferenceNode();
			
			root.getTarget().generateArgumentReference(builder, node()).append("->").append(VTable.IDENTIFIER).append("->");
			
			VirtualMethodDeclaration virtual = node().getMethodDeclaration().getVirtualMethod();
			
			builder.append(virtual.getTarget().generateVirtualMethodName());
		}
		else
		{
			VariableDeclaration d = node().getDeclaration();
			
			builder.append('&').append(d.getTarget().generateSourceName());
		}
		
		builder.append(", ");
		
		decl.getTarget().generateArguments(builder, node(), node().getMethodDeclaration());
		
		return builder;
	}
}
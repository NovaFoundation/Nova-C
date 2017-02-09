package nova.c.nodewriters;

import net.fathomsoft.nova.Nova;
import net.fathomsoft.nova.tree.ClosureContext;
import net.fathomsoft.nova.tree.ClosureDeclaration;
import net.fathomsoft.nova.tree.ClosureVariableDeclaration;
import net.fathomsoft.nova.tree.variables.VariableDeclaration;

public abstract class ClosureVariableDeclarationWriter extends VariableDeclarationWriter
{
	public abstract ClosureVariableDeclaration node();
	
	@Override
	public StringBuilder generateTypeName(StringBuilder builder)
	{
		return getWriter(node().originalDeclaration).generateTypeName(builder);
	}
	
	public String getHeapVariableName()
	{
		return "heap" + ((ClosureContext)node().getAncestorOfType(ClosureContext.class)).id + "_" + node().getIndex();
	}
	
	@Override
	public StringBuilder generateSourceName(StringBuilder builder, String uniquePrefix)
	{
		return generateIdentifierSourceName(builder, uniquePrefix);
	}
	
	public StringBuilder generateLeftAssignment(StringBuilder builder)
	{
		return builder.append(((ClosureContext)node().parent).declaration.getName()).append("->");
	}
	
	public StringBuilder generateDeclarationValue(StringBuilder builder)
	{
		//Variable v = var.generateUsableVariable(node(), Location.INVALID);
		
		if (node().originalDeclaration instanceof ClosureVariableDeclaration)
		{
			builder.append("context->");
		}
		else if (!node().originalDeclaration.isAllocatedOnHeap())
		{
			builder.append('&');
		}
		
		return getWriter(node().originalDeclaration).generateIdentifierSourceName(builder, null);
	}
	
	public StringBuilder generateAssignment(StringBuilder builder)
	{
		VariableDeclaration root = node().getRootDeclaration();
		
		boolean heap = node().requiresHeapAllocation && root instanceof ClosureDeclaration == false;
		
		String heapName = null;
		
		if (heap)
		{
			heapName = getHeapVariableName();
			
			getWriter(node().originalDeclaration).generateType(builder).append("* ").append(heapName).append(" = (")
				.append(getWriter(node().originalDeclaration).generateType()).append("*)NOVA_MALLOC(sizeof(").append(getWriter(node().originalDeclaration).generateType()).append("));\n");
			
			builder.append("*").append(heapName).append(" = ").append(getWriter(node().originalDeclaration).generateSourceName()).append(";\n");
		}
		
		generateLeftAssignment(builder);
		generateSourceName(builder).append(" = ");
		
		if (heap)
		{
			builder.append(heapName);
		}
		else
		{
			generateDeclarationValue(builder);
		}
		
		builder.append(";\n");
		
		Nova.debuggingBreakpoint(node().getName().equals("func"));
		
		if (root instanceof ClosureDeclaration)
		{
			String context = node().originalDeclaration != root ? "context->" : "";
			
			generateLeftAssignment(builder);
			getWriter(root).generateObjectReferenceIdentifier(builder).append(" = ").append(context);
			getWriter(root).generateObjectReferenceIdentifier(builder).append(";\n");
			
			generateLeftAssignment(builder);
			builder.append(root.getContextName()).append(" = ").append(context);
			builder.append(root.getContextName()).append(";\n");
		}
		
		return builder;
	}
	
	@Override
	public StringBuilder generateType(StringBuilder builder, boolean checkArray, boolean checkValueReference, boolean checkAllocatedOnHeap)
	{
		super.generateType(builder, checkArray, checkValueReference, checkAllocatedOnHeap);
		
		if (checkAllocatedOnHeap && node().requiresHeapAllocation)
		{
//			builder.append("*");
		}
		
		return builder;
	}
}
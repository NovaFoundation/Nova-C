package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class VirtualMethodDeclarationWriter extends BodyMethodDeclarationWriter
{
	public abstract VirtualMethodDeclaration node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		generateSourceSignature(builder);
		
            /*
            if (getType() == null)
            {
                builder.append("{}");
            }
            else
            {
                builder.append("{return 0;}");
            }
            */
		
		builder.append("\n{\n");
		
		if (node().getType() != null)
		{
			builder.append("return ");
		}
		
		Parameter ref = node().getOriginalParameterList().getObjectReference();
		
		ref.getTarget().generateSourceFragment(builder).append("->");
		
		builder.append(VTable.IDENTIFIER).append("->");
		
		if (node().getParentClass() instanceof Interface)
		{
			builder.append(InterfaceVTable.IDENTIFIER).append(".");
		}
		
		String call = node().getName() + "(";
		
		for (int i = 0; i < node().getParameterList().getNumVisibleChildren(); i++)
		{
			if (i > 0)
			{
				call += ", ";
			}
			
			call += node().getParameterList().getVisibleChild(i).getName();
		}
		
		call += ")";
		
		MethodCall output = MethodCall.decodeStatement(node().getScope(), call, node().getLocationIn().asNew(), true, true, node());
		
		generateVirtualMethodName(builder);
		output.getArgumentList().getTarget().generateSourceFragment(builder);
		
		return builder.append(";\n}\n");
	}
	
	public StringBuilder generateSourceName(StringBuilder builder, String uniquePrefix)
	{
		return generateVirtualMethodName(builder);
	}
	
	/**
	 * Get the identifier for the virtual abstract method in the vtable.
	 *
	 * @return The identifier for the virtual method in the vtable.
	 */
	public StringBuilder generateVirtualMethodName()
	{
		return generateVirtualMethodName(new StringBuilder());
	}
	
	/**
	 * Get the identifier for the virtual abstract method in the vtable.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The identifier for the virtual method in the vtable.
	 */
	public StringBuilder generateVirtualMethodName(StringBuilder builder)
	{
		String prefix = "virtual";
		
		if (node().base instanceof PropertyMethod)
		{
			prefix += "_" + ((PropertyMethod)node().base).getMethodPrefix();
		}
		
		return generateSourceName(builder, prefix, true);
	}
}
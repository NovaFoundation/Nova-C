package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

import java.io.IOException;
import java.io.PrintWriter;

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
		
		getWriter(ref).generateSourceFragment(builder).append("->");
		
		builder.append(VTable.IDENTIFIER).append("->");
		
		if (node().getParentClass() instanceof Trait)
		{
			builder.append(TraitVTable.IDENTIFIER).append(".");
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
		getWriter(output.getArgumentList()).generateSourceFragment(builder);
		
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
	
	public java.io.Writer writeVTableDeclaration(java.io.Writer writer) throws IOException
	{
		writer.write(generateType().toString());
		writer.write(" (*");
		writer.write(generateVirtualMethodName().toString());
		writer.write(")(");
		writer.write(getWriter(node().getParameterList()).generateHeader().toString());
		writer.write(");\n");
		
		return writer;
	}
}
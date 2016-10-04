package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class LocalDeclarationWriter extends VariableDeclarationWriter
{
	public abstract LocalDeclaration node();
	
	public StringBuilder generateType(StringBuilder builder, boolean checkArray, boolean checkValueReference)
	{
		if (node().isImplicit())
		{
                /*builder.append("void*");
                
                if (checkValueReference && isValueReference())
                {
                    builder.append('*');
                }
                
                return builder;*/
			return node().implicitType.getTarget().generateType(builder, checkArray, checkValueReference);
		}
		
		return super.generateType(builder, checkArray, checkValueReference);
	}
}
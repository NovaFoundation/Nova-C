package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ExtensionVTableWriter extends VTableWriter
{
	public abstract ExtensionVTable node();
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		InterfaceVTable table = node().getInterfaceVTable();
		
		getWriter(table).generateHeader(builder).append('\n');
		
		super.generateHeader(builder);
		
		builder.append("extern ").append(generateType()).append(' ').append(generateSourceName()).append(";\n");
		
		return builder;
	}
	
	@Override
	public StringBuilder writeChildrenHeader(StringBuilder builder)
	{
		ClassDeclarationWriter clazz = getWriter(node().getProgram().getClassDeclaration("nova/Class"));
		
		clazz.generateType(builder).append(" ").append(clazz.getClassInstanceVTableName()).append(";\n");
		
		return super.writeChildrenHeader(builder);
	}
	
	@Override
	public StringBuilder writeChildrenSource(StringBuilder builder)
	{
		builder.append("0,\n");
		
		return super.writeChildrenSource(builder);
	}
}
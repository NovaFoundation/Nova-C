package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

public abstract class ImportListWriter extends ListWriter
{
	public abstract ImportList node();
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			child.getTarget().generateSource(builder);
		}
		
		return builder;
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		FileDeclaration file = node().getFileDeclaration();
		
		Import importNode = Import.decodeStatement(node(), "import \"" + file.getClassDeclaration().getClassLocation() + "\"", node().getLocationIn(), true);
		
		return importNode.getTarget().generateSource(builder);
	}
}
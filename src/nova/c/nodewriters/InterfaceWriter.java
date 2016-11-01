package nova.c.nodewriters;

import net.fathomsoft.nova.tree.*;

import java.io.PrintWriter;

public abstract class InterfaceWriter extends ClassDeclarationWriter
{
	public abstract Interface node();
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		return super.generateSource(builder);
	}
	
	public PrintWriter writeVTableDeclaration(PrintWriter writer)
	{
		writer.print(getVTableType() + " " + getVTableValueName() + ";\n");
		
		return writer;
	}
	
	public PrintWriter writeDefaultVTable(PrintWriter writer)
	{
		writer.print(getVTableTypeName() + " " + getVTableDefaultValueName() + " = {");
		
		NovaMethodDeclaration[] methods = node().getInterfaceVirtualMethods(false);
		
		if (methods.length > 0)
		{
			for (int i = 0; i < methods.length; i++)
			{
				if (i > 0)
				{
					writer.print(",");
				}
				
				writer.print("0");
			}
		}
		else
		{
			writer.write("0");
		}
		
		writer.print("};\n");
		
		return writer;
	}
	
	public PrintWriter writeVTableAssignment(PrintWriter writer)
	{
		writer.print("struct " + getVTableTypeName() + " {\n");
		
		NovaMethodDeclaration[] methods = node().getInterfaceVirtualMethods(false);
		
		if (methods.length > 0)
		{
			for (NovaMethodDeclaration m : methods)
			{
				getWriter(m.getVirtualMethod()).writeVTableDeclaration(writer);
			}
		}
		else
		{
			writer.write("char x;\n");
		}
		
		writer.print("};\n");
		
		return writer;
	}
	
	public String getVTableValueName()
	{
		return generateSourceName("vtable").toString() + "_value";
	}
	
	public String getVTableDefaultValueName()
	{
		return generateSourceName("vtable").toString() + "_value_default";
	}
	
	public String getVTableTypeName()
	{
		return generateSourceName("vtable").toString();
	}
	
	public String getVTableType()
	{
		return getVTableTypeName() + "*";
	}
	
	public PrintWriter writeVTableTypedef(PrintWriter writer)
	{
		String name = getVTableTypeName();
		
		writer.print("typedef struct " + name + " " + name + ";\n");
		
		return writer;
	}
	
	public PrintWriter writeDefaultVTableDeclaration(PrintWriter writer)
	{
		writer.print("extern " + getVTableTypeName() + " " + getVTableDefaultValueName() + ";\n");
		
		return writer;
	}
}
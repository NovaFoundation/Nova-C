package nova.c.nodewriters;

import net.fathomsoft.nova.Nova;
import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.Package;
import net.fathomsoft.nova.util.SyntaxUtils;

import java.util.ArrayList;

public abstract class FileDeclarationWriter extends NodeWriter
{
	public abstract FileDeclaration node();
	
	/**
	 * Get the name of the file that will be output as a C header file.<br>
	 * <br>
	 * For example: A generateHeaderName() call for a FileDeclaration of Test.nova
	 * would return "nova_Test.h"
	 *
	 * @return The name of the file output as a C header file.
	 */
	public String generateHeaderName()
	{
		Package pkg = node().getPackage();
		ClassDeclaration clazz = node().getClassDeclaration();
		
		return pkg.getTarget().generateHeaderLocation() + "/" + clazz.getTarget().generateSourceName() + ".h";
	}
	
	/**
	 * Get the name of the file that will be output as a C source file.<br>
	 * <br>
	 * For example: A generateSourceName() call for a FileDeclaration of Test.nova
	 * would return "nova_Test.c"
	 *
	 * @return The name of the file output as a C source file.
	 */
	public String generateSourceName()
	{
		Package pkg = node().getPackage();
		ClassDeclaration clazz = node().getClassDeclaration();
		
		return pkg.getTarget().generateHeaderLocation() + "/" + clazz.getTarget().generateSourceName() + ".c";
	}
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		if (node().header == null)
		{
			ClassDeclaration clazz = node().getClassDeclaration();
			
			String definitionName = "FILE_" + clazz.getTarget().generateSourceName() + "_" + Nova.LANGUAGE_NAME.toUpperCase();
			
			builder.append("#pragma once").append('\n');
			builder.append("#ifndef ").append(definitionName).append('\n');
			builder.append("#define ").append(definitionName).append("\n\n");
			
			generateDummyTypes(builder).append('\n');
			
			generateClosureDefinitions(builder, true).append('\n');
			
			ImportList imports = node().getImportList();
			
			imports.getTarget().generateHeader(builder);
			
			builder.append('\n');
			
			for (int i = 0; i < node().getNumChildren(); i++)
			{
				Node child = node().getChild(i);
				
				if (child != imports)
				{
					child.getTarget().generateHeader(builder);
				}
			}
			
			builder.append('\n').append("#endif").append('\n');
			
			node().header = builder;
		}
		
		return node().header;
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		if (node().source == null)
		{
			builder.append("#include <precompiled.h>\n");
			
			ImportList imports = node().getImportList();
			
			imports.getTarget().generateSource(builder).append('\n');
			
			generateSourceClosureContextDefinitions(builder).append('\n');
			generateClosureDefinitions(builder, false).append('\n');
			
			for (int i = 0; i < node().getNumChildren(); i++)
			{
				Node child = node().getChild(i);
				
				if (child != node().getImportList())
				{
					child.getTarget().generateSource(builder);
				}
			}
			
			node().source = builder.append('\n');
		}
		
		return node().source;
	}
	
	public StringBuilder generateHeaderNativeInterface(StringBuilder builder)
	{
		for (ClassDeclaration clazz : node().getClassDeclarations())
		{
			clazz.getTarget().generateHeaderNativeInterface(builder);
		}
		
		return builder;
	}
	
	public StringBuilder generateSourceNativeInterface(StringBuilder builder)
	{
		for (ClassDeclaration clazz : node().getClassDeclarations())
		{
			clazz.getTarget().generateSourceNativeInterface(builder);
		}
		
		return builder;
	}
	
	/**
	 * Generate dummy class declarations for each of the imported files.
	 * node() is needed in a situation when a class imports a class that
	 * in returns needs to import the respective one. In other words,
	 * the chicken vs egg scenario.
	 *
	 * @return The generated code used for generating the dummy class
	 * 		types.
	 */
	private StringBuilder generateDummyTypes(StringBuilder builder)
	{
		//		builder.append("typedef struct ExceptionData ExceptionData;\n");
		
		for (int i = 0; i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			if (child instanceof ClassDeclaration)
			{
				ClassDeclaration clazz = (ClassDeclaration)child;
				
				builder.append("typedef struct ").append(clazz.getTarget().generateSourceName()).append(' ').append(clazz.getTarget().generateSourceName()).append(';').append('\n');
			}
		}
		
		//		ImportList imports = getImportList();
		//		
		//		for (int i = 0; i < imports.getNumChildren(); i++)
		//		{
		//			Import node = (Import)imports.getChild(i);
		//			
		//			if (!node().isExternal())
		//			{
		//				String name = node().getLocationNode().getName();
		//				
		//				builder.append("typedef struct ").append(name).append(' ').append(name).append(';').append('\n');
		//			}
		//		}
		
		return builder;
	}
	
	private StringBuilder generateSourceClosureContextDefinitions(StringBuilder builder)
	{
		for (ClosureContext context : node().contexts)
		{
			context.getTarget().generateSource(builder);
		}
		
		return builder;
	}
	
	/**
	 * Generate the type definitions for the closures used within the
	 * file.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @param publicClosures Whether to generate the definitions for the
	 * 		public closures, or the private ones.
	 * @return The StringBuilder with the appended data.
	 */
	private StringBuilder generateClosureDefinitions(StringBuilder builder, boolean publicClosures)
	{
		ArrayList<String> types = new ArrayList<>();
		
		for (ClosureDeclaration closure : node().closures)
		{
			if (closure.isPublic() == publicClosures)
			{
				SyntaxUtils.addTypesToTypeList(builder, closure, types);
			}
		}
		
		if (types.size() > 0)
		{
			builder.append('\n');
		}
		
		for (ClosureDeclaration closure : node().closures)
		{
			if (closure.isPublic() == publicClosures)
			{
				closure.getTarget().generateClosureDefinition(builder);
			}
		}
		
		return builder;
	}
	
	/**
	 * Format the C Header output, if the output has been generated.
	 */
	public void formatHeaderOutput()
	{
		if (node().header == null)
		{
			return;
		}
		
		node().setHeader(SyntaxUtils.formatText(node().header.toString()));
	}
	
	/**
	 * Format the C Source output, if the output has been generated.
	 */
	public void formatSourceOutput()
	{
		if (node().source == null)
		{
			return;
		}
		
		node().setSource(SyntaxUtils.formatText(node().source.toString()));
	}
}
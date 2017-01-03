package nova.c.engines;

import net.fathomsoft.nova.CodeGeneratorEngine;
import net.fathomsoft.nova.Nova;
import net.fathomsoft.nova.error.SyntaxMessage;
import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.annotations.NativeAnnotation;
import net.fathomsoft.nova.tree.exceptionhandling.Exception;
import net.fathomsoft.nova.tree.variables.FieldDeclaration;
import net.fathomsoft.nova.util.FileUtils;
import net.fathomsoft.nova.util.StringUtils;
import net.fathomsoft.nova.util.SyntaxUtils;
import nova.c.nodewriters.ClassDeclarationWriter;
import nova.c.nodewriters.FileDeclarationWriter;
import nova.c.nodewriters.StaticBlockWriter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static net.fathomsoft.nova.Nova.*;
import static nova.c.nodewriters.NodeWriter.getWriter;

public class CCodeGeneratorEngine extends CodeGeneratorEngine
{
	private ArrayList<File>		cSourceFiles, cHeaderFiles;
	
	public File					nativeInterfaceSource, nativeInterfaceHeader;
	public File					interfaceVTableHeader, vtableDeclarationsHeader, vtableDeclarationsSource;
	
	private static final String NATIVE_INTERFACE_FILE_NAME = "NovaNativeInterface";
	private static final String INTERFACE_VTABLE_FILE_NAME = "InterfaceVTable";
	private static final String MAIN_FUNCTION_FILE_NAME = "MainFunction";
	private static final String VTABLE_DECLARATIONS_FILE_NAME = "VTableDeclarations";
	private static final String ENVIRONMENT_VAR            = "novaEnv";
	
	private CCompileEngine compileEngine;
	
	public static final HashSet<String> KEYWORDS = new HashSet<String>() {{
		add("auto");
		add("break");
		add("case");
		add("char");
		add("const");
		add("continue");
		add("default");
		add("do");
		add("double");
		add("else");
		add("enum");
		add("extern");
		add("float");
		add("for");
		add("goto");
		add("if");
		add("int");
		add("long");
		add("register");
		add("return");
		add("short");
		add("signed");
		add("sizeof");
		add("static");
		add("struct");
		add("switch");
		add("typedef");
		add("union");
		add("unsigned");
		add("void");
		add("volatile");
		add("while");
	}};
	
	public CCodeGeneratorEngine(Nova controller)
	{
		super(controller);
		
		cSourceFiles = new ArrayList<>();
		cHeaderFiles = new ArrayList<>();
	}
	
	public void init()
	{
		compileEngine = (CCompileEngine)controller.compileEngine;
	}
	
	/**
	 * Generate the C Header output from the data contained within the
	 * syntax tree.
	 */
	public void generateCHeaderOutput()
	{
		getWriter(tree.getRoot()).generateHeader(new StringBuilder());
	}
	
	/**
	 * Generate the C Source output from the data contained within the
	 * syntax tree.
	 */
	public void generateCSourceOutput()
	{
		getWriter(tree.getRoot()).generateSource(new StringBuilder());
	}
	
	/**
	 * Generate the C Source and Header output from the data contained
	 * within the syntax tree.
	 */
	public void generateOutput()
	{
		generateCHeaderOutput();
		generateCSourceOutput();
	}
	
	public void formatOutput()
	{
		getWriter(tree.getRoot()).formatSourceOutput();
		getWriter(tree.getRoot()).formatHeaderOutput();
	}
	
	/**
	 * Get the C Header output text (destination text) from the Syntax
	 * tree.
	 *
	 * @return The C Header output text after compilation.
	 */
	public String[] getCHeaderOutput()
	{
		Program root = tree.getRoot();
		
		String headers[] = new String[root.getNumChildren()];
		
		for (int i = 0; i < headers.length; i++)
		{
			Node child = root.getChild(i);
			
			headers[i] = getWriter(child).generateHeader().toString();
		}
		
		return headers;
	}
	
	/**
	 * Get the C Source output text (destination text) from the Syntax
	 * tree.
	 *
	 * @return The C Source output text after compilation.
	 */
	public String[] getCSourceOutput()
	{
		Program root = tree.getRoot();
		
		String sources[] = new String[root.getNumChildren()];
		
		for (int i = 0; i < sources.length; i++)
		{
			Node child = root.getChild(i);
			
			sources[i] = getWriter(child).generateSource().toString();
		}
		
		return sources;
	}
	
	public void writeFiles()
	{
		generateVTableDeclarations(); // must do first because then sets forceRecompile
		
		generateNativeInterface();
		generateInterfaceVTable();

		writeMakefile();

		String headers[] = getCHeaderOutput();
		String sources[] = getCSourceOutput();
		FileDeclaration files[] = tree.getFiles();
		
		if (controller.isFlagEnabled(CSOURCE))
		{
			for (int i = 0; i < headers.length; i++)
			{
				controller.log(headers[i]);
				controller.log(sources[i]);
			}
		}
		
		StringBuilder allHeaders = new StringBuilder();
		StringBuilder includes   = new StringBuilder();
		StringBuilder types      = new StringBuilder();
		
		allHeaders.append("#pragma once\n");
		allHeaders.append("#ifndef NOVA_ALL_HEADERS\n");
		allHeaders.append("#define NOVA_ALL_HEADERS\n\n");
		allHeaders.append("#include <Nova.h>\n");
		allHeaders.append("#include <math.h>\n");
		allHeaders.append("#include <ExceptionHandler.h>\n");
		allHeaders.append("#include <setjmp.h>\n").append('\n');
		
		for (int i = 0; i < files.length; i++)
		{
			FileDeclaration file   = files[i];
			String          header = headers[i];
			String          source = sources[i];
			
			File outputDir = getOutputDirectory(file);
			
			new File(outputDir, file.getPackage().getLocation()).mkdirs();
			
			types.append("typedef struct ").append(file.getName()).append(' ').append(file.getName()).append(';').append('\n');
			includes.append("#include <").append(getWriter(file).generateHeaderName()).append('>').append('\n');
			
			try
			{
				if (!controller.isFlagEnabled(NO_C_OUTPUT))
				{
					File headerFile = new File(outputDir, getWriter(file).generateHeaderName());
					File sourceFile = new File(outputDir, getWriter(file).generateSourceName());
					
					if (compileEngine.forceRecompile || file.getFile().lastModified() > headerFile.lastModified())
					{
						if (FileUtils.writeIfDifferent(headerFile, header))
						{
							controller.log("Wrote " + headerFile.getCanonicalPath());
						}
					}
					if (compileEngine.forceRecompile || file.getFile().lastModified() > sourceFile.lastModified())
					{
						if (FileUtils.writeIfDifferent(sourceFile, source))
						{
							controller.log("Wrote " + sourceFile.getCanonicalPath());
						}
					}
					
					cHeaderFiles.add(headerFile);
					cSourceFiles.add(sourceFile);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
				
				controller.completed(false);
			}
		}
		
		allHeaders.append(types).append('\n');
		allHeaders.append(includes).append('\n');
		
		allHeaders.append("#endif");
		
		writeClassData();
	}
	
	public Interface[] getAllInterfaces()
	{
		ArrayList<Interface> list = new ArrayList<>();
		
		for (FileDeclaration file : tree.getFiles())
		{
			if (file.getClassDeclaration() instanceof Interface)
			{
				list.add((Interface)file.getClassDeclaration());
			}
		}
		
		return list.toArray(new Interface[0]);
	}
	
	public ClassDeclaration[] getAllClasses()
	{
		return getAllClasses(true);
	}
	
	public ClassDeclaration[] getAllClasses(boolean includeInterfaces)
	{
		ArrayList<ClassDeclaration> list = new ArrayList<>();
		
		for (FileDeclaration file : tree.getFiles())
		{
			if (includeInterfaces || file.getClassDeclaration() instanceof Interface == false)
			{
				list.add(file.getClassDeclaration());
			}
		}
		
		return list.toArray(new ClassDeclaration[0]);
	}
	
	public VirtualMethodDeclaration[] getAllVirtualMethods()
	{
		ArrayList<VirtualMethodDeclaration> list = new ArrayList<>();
		
		for (ClassDeclaration c : getAllClasses())
		{
			for (NovaMethodDeclaration method : c.getExtensionVirtualMethods(false))
			{
				VirtualMethodDeclaration virtual = method.getVirtualMethod();
				
				if (virtual != null && !list.contains(virtual))
				{
					list.add(virtual);
				}
			}
		}
		
		return list.toArray(new VirtualMethodDeclaration[0]);
	}
	
	public boolean writeMakefile()
	{
		try
		{
			File makefile = new File(controller.outputDirectory, "makefile");

//			long lastModified = makefile.exists() ? makefile.lastModified() : 0;
			
			FileUtils.writeIfDifferent(makefile, writer ->
			{
				writer.print("NOVA_DEPS =");
				
				for (FileDeclaration file : tree.getFiles())
				{
					writer.print(" " + getWriter(file).generateFullLocation() + ".h");
				}
				
				for (String external : controller.externalImports)
				{
					Path outputPath = Paths.get(controller.outputDirectory.toURI());
					Path targetPath = Paths.get(new File(external).toURI());
					
					String relative = outputPath.relativize(targetPath).toString().replace("\\", "/");
					
					writer.print(" " + relative.substring(0, relative.length() - 2) + ".h");
				}
				
				writer.print("\n");
				
				writer.print("NOVA_OBJ =");
				
				for (FileDeclaration file : tree.getFiles())
				{
					writer.print(" " + getWriter(file).generateFullLocation() + ".o");
				}
				
				for (String external : controller.externalImports)
				{
					Path outputPath = Paths.get(controller.outputDirectory.toURI());
					Path targetPath = Paths.get(new File(external).toURI());
					
					String relative = outputPath.relativize(targetPath).toString().replace("\\", "/");
					
					if (new File(external.substring(0, external.length() - 2) + ".c").exists())
					{
						writer.print(" " + relative.substring(0, relative.length() - 2) + ".o");
					}
				}
				
				writer.print("\n\n");
				
				try
				{
					Path outputPath = Paths.get(controller.outputDirectory.getCanonicalPath());
					Path targetPath = Paths.get(controller.targetEngineWorkingDir.getCanonicalPath());
					
					String relative = outputPath.relativize(targetPath).toString().replace("\\", "/");
					
					writer.print("NOVA_COMPILE_HOME = ");
					writer.print(relative.length() > 0 ? relative : ".");
					writer.print("\n");
					
					targetPath = Paths.get(controller.targetEngineWorkingDir.getParentFile().getCanonicalPath());
					relative = outputPath.relativize(targetPath).toString().replace("\\", "/");
					
					writer.print("NOVA_STDLIB_LOCATION = ");
					writer.print(relative.length() > 0 ? relative : ".");
					writer.print("/StandardLibrary\n\n");
					
					writer.print("MAKEFILE_LOCATION = ");
					writer.print("$(NOVA_COMPILE_HOME)/makefile.nova\n\n");
					
					writer.print("include $(MAKEFILE_LOCATION)");
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}, compileEngine.forceRecompile);
			
//			if (lastModified > 0)
//			{
//				makefile.setLastModified(lastModified);
//			}
		}
		catch (IOException e)
		{
			return false;
		}
		
		return true;
	}
	
	public boolean writeClassData()
	{
		try
		{
			final Interface[] interfaces = getAllInterfaces();
			
			File header = new File(controller.outputDirectory, "NovaClassData.h");
			
			FileUtils.writeIfDifferent(header, writer ->
			{
				writer.print("#ifndef NOVA_CLASS_DATA\n#define NOVA_CLASS_DATA\n\n");
				
				ClassDeclaration clazz = controller.getTree().getRoot().getClassDeclaration("nova/meta/Class");
				
				writer.print("typedef struct NovaClassData NovaClassData;\n\n");
				
				try
				{
					for (Interface i : interfaces)
					{
						getWriter(i).writeVTableTypedef(writer);
					}
					
					for (Interface i : interfaces)
					{
						getWriter(i).writeDefaultVTableDeclaration(writer);
					}
					
					writer.print("\n");
					writer.write(getAllIncludes());
					writer.print("\n");
					
					for (Interface i : interfaces)
					{
						getWriter(i).writeVTableAssignment(writer);
						writer.print("\n");
					}
					
					writer.print("\n");
					
					writer.print("\nstruct NovaClassData {\n");
					
					//writer.print(clazzWriter.generateType().toString() + " instance_class;\n\n");
					
					for (Interface i : interfaces)
					{
						getWriter(i).writeVTableDeclaration(writer);
					}
					
					writer.print("\n");
					
					for (VirtualMethodDeclaration virtual : getAllVirtualMethods())
					{
						getWriter(virtual).writeVTableDeclaration(writer);
					}
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
				
				writer.print("};\n");
				
				writer.print("\n#endif");
			}, compileEngine.forceRecompile);
			
			File source = new File(controller.outputDirectory, "NovaClassData.c");
			
			FileUtils.writeIfDifferent(source, writer ->
			{
				writer.write("#include <NovaClassData.h>\n\n");
				
				try
				{
					for (Interface i : interfaces)
					{
						getWriter(i).writeDefaultVTable(writer);
					}
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}, compileEngine.forceRecompile);
		}
		catch (IOException e)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * Get the directory that holds the Nova library.
	 *
	 * @return The location of the directory that holds the library.
	 */
	private String getLibraryDir()
	{
		return FileUtils.formatPath(controller.targetEngineWorkingDir.getAbsolutePath() + "/lib");
	}
	
	/**
	 * Get the directory that holds the Nova include files.
	 *
	 * @return The location of the directory that holds the include files.
	 */
	private String getIncludeDir()
	{
		return FileUtils.formatPath(controller.targetEngineWorkingDir.getAbsolutePath() + "/include");
	}
	
	private void generateNativeInterface()
	{
		generateNativeInterfaceHeader();
		generateNativeInterfaceSource();
	}
	
	public String getAllIncludes()
	{
		StringBuilder builder = new StringBuilder();
		
		for (FileDeclaration file : tree.getFiles())
		{
			builder.append(getWriter(file).getIncludeStatement()).append("\n");
		}
		
		return builder.toString();
	}
	
	private void generateNativeInterfaceHeader()
	{
		File header = new File(controller.outputDirectory, NATIVE_INTERFACE_FILE_NAME + ".h");
		
		controller.outputDirectory.mkdirs();
		
		try
		{
			FileUtils.writeIfDifferent(header, writer ->
			{
				writer.append("#ifndef NOVA_NATIVE_INTERFACE\n");
				writer.append("#define NOVA_NATIVE_INTERFACE\n\n");
				
				writer.append(getAllIncludes());
				
				writer.append('\n');
				
				for (FileDeclaration file : tree.getFiles())
				{
					writer.print(getWriter(file).generateHeaderNativeInterface(new StringBuilder()).append("\n"));
				}
				
				writer.append("\ntypedef struct nova_env\n");
				writer.append("{\n");
				
				for (FileDeclaration file : tree.getFiles())
				{
					for (ClassDeclaration clazz : file.getClassDeclarations())
					{
						writer.print(getWriter(clazz).generateSourceName(new StringBuilder(), "native").append(" ").append(clazz.getNativeLocation()).append(";\n"));
					}
				}
				
				writer.append("} nova_env;\n\n");
				writer.append("extern nova_env " + ENVIRONMENT_VAR + ";\n\n");
				writer.append("\n#endif\n");
			}, compileEngine.forceRecompile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void generateNativeInterfaceSource()
	{
		File source = new File(controller.outputDirectory, NATIVE_INTERFACE_FILE_NAME + ".c");
		
		try
		{
			FileUtils.writeIfDifferent(source, writer ->
			{
				writer.append("#include \"" + NATIVE_INTERFACE_FILE_NAME + ".h\"\n\n");
				
				writer.append("nova_env " + ENVIRONMENT_VAR + " = {\n");
				
				for (FileDeclaration file : tree.getFiles())
				{
					writer.print(getWriter(file).generateSourceNativeInterface(new StringBuilder()).append('\n'));
				}
				
				writer.append("};\n");
			}, compileEngine.forceRecompile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void generateInterfaceVTable()
	{
		File header = new File(controller.outputDirectory, INTERFACE_VTABLE_FILE_NAME + ".h");
		
		try
		{
			FileUtils.writeIfDifferent(header, writer ->
			{
				writer.append(generateInterfaceVTableHeader());
			}, compileEngine.forceRecompile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private String generateInterfaceVTableHeader()
	{
		StringBuilder builder = new StringBuilder();
		
		NovaMethodDeclaration[] methods = tree.getRoot().getProgram().getInterfaceMethods();
		ClosureDeclaration[] closures = tree.getRoot().getProgram().getPublicClosures();
		
		builder.append("#ifndef NOVA_INTERFACE_VTABLE\n");
		builder.append("#define NOVA_INTERFACE_VTABLE\n\n");
		
		ArrayList<String> types = new ArrayList<String>();
		
		for (NovaMethodDeclaration method : methods)
		{
			FileDeclarationWriter.generateTypeDefinition(builder, method.getParentClass(), types);
			FileDeclarationWriter.generateTypeDefinition(builder, method, types);
			
			FileDeclarationWriter.addTypesToTypeList(builder, method, types);
		}
		
		for (ClosureDeclaration c : closures)
		{
			FileDeclarationWriter.addTypesToTypeList(builder, c, types);
			
			getWriter(c).generateClosureDefinition(builder);
		}
		
		builder.append("\n");
		builder.append("typedef struct ").append(InterfaceVTable.TYPE).append("\n");
		builder.append("{\n");
		
		for (NovaMethodDeclaration method : methods)
		{
			getWriter(method.getVirtualMethod()).generateInterfaceVTableHeader(builder);
		}
		
		builder.append("} ").append(InterfaceVTable.TYPE).append(";\n");
		
		builder.append("\n");
		builder.append("#endif\n");
		
		return builder.toString();
	}
	
	private StringBuilder generateVTableClassInstanceAssignments(NovaMethodDeclaration method)
	{
		StringBuilder builder = new StringBuilder();
		
		for (ClassDeclaration c : getAllClasses())
		{
			getWriter(c).generateVTableClassInstanceAssignment(builder, method);
		}
		
		return builder.append("\n");
	}
	
	private StringBuilder generateVTableClassInstancePropertyAssignments()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("nova_Nova_Object** nova_class_interfaces;\n\n");
		
		for (ClassDeclaration c : getAllClasses())
		{
			getWriter(c).generateVTableClassPropertyAssignments(builder);
		}
		
		return builder.append("\n");
	}
	
	private StringBuilder generateVTableClassArray()
	{
		StringBuilder builder = new StringBuilder();
		
		ClassDeclaration[] classes = getAllClasses();
		
		ClassDeclaration classClass = tree.getRoot().getClassDeclaration("nova/meta/Class");
		ClassDeclaration arrayClass = tree.getRoot().getClassDeclaration("nova/datastruct/list/ImmutableArray");
		
		ClassDeclarationWriter clazz = getWriter(classClass);
		
		String name = "nova_all_classes";
		
		builder.append(clazz.generateSourceName()).append("** ").append(name).append(" = NOVA_MALLOC(sizeof(").append(clazz.generateSourceName()).append("*) * ").append(classes.length).append(");\n");
		
		int i = 0;
		
		for (ClassDeclaration c : classes)
		{
			builder.append(name).append("[").append(i++).append("] = ").append(getWriter(c).getVTableClassInstance()).append(";\n");
		}
		
		FieldDeclaration allArray = classClass.getField("ALL", false);
		
		NovaMethodDeclaration[] constructors = arrayClass.getConstructorList().getMethods();
		
		NovaMethodDeclaration method = null;
		
		for (NovaMethodDeclaration c : constructors)
		{
			if (c.getParameterList().getNumParameters() == 2 && c.getParameter(0).isPrimitiveArray())
			{
				method = c;
			}
		}
		
		builder.append(getWriter(allArray).generateSourceName()).append(" = ")
			.append(getWriter(method).generateSourceName()).append("(0, ").append(Exception.EXCEPTION_DATA_IDENTIFIER).append(", (nova_Nova_Object**)").append(name).append(", ").append(classes.length).append(");\n");
		
		return builder.append("\n");
	}
	
	private StringBuilder generateNativeVirtualMethodAssignments()
	{
		StringBuilder builder = new StringBuilder();
		
		Program root = tree.getRoot();
		
		for (int i = 0; i < root.getNumVisibleChildren(); i++)
		{
			FileDeclaration file = root.getVisibleChild(i);
			
			for (ClassDeclaration clazz : file.getClassDeclarations())
			{
				MethodDeclaration[] methods = clazz.getVisibleNativeMethods();
				
				for (MethodDeclaration method : methods)
				{
					if (method instanceof NovaMethodDeclaration)
					{
						if (method.isInstance())
						{
							NovaMethodDeclaration n = (NovaMethodDeclaration)method;
							
							if (n.isOverridden() && !(n instanceof Constructor))
							{
								//n = n.getVirtualMethod();
								
								String itable = "";
								
								if (n.getRootDeclaration().getParentClass() instanceof Interface)
								{
									itable = InterfaceVTable.IDENTIFIER + ".";
								}
								
								VirtualMethodDeclaration virtual = n.getVirtualMethod();
								
								builder.append(ENVIRONMENT_VAR + "." + clazz.getNativeLocation() + "." + getWriter(n).generateSourceNativeName(new StringBuilder(), false) + " = " + clazz.getVTableNodes().getExtensionVTable().getName() + "." + itable + getWriter(virtual).generateVirtualMethodName() + ";\n");
							}
						}
					}
				}
			}
		}
		
		return builder;
	}

	private void generateVTableDeclarations()
	{
		File header = new File(controller.outputDirectory, VTABLE_DECLARATIONS_FILE_NAME + ".h");
		File source = new File(controller.outputDirectory, VTABLE_DECLARATIONS_FILE_NAME + ".c");
		
		try
		{
			boolean forceRecompile = compileEngine.forceRecompile;
			
			forceRecompile = forceRecompile | FileUtils.writeIfDifferent(source, writer ->
			{
				writer.append("#include \"" + VTABLE_DECLARATIONS_FILE_NAME + ".h\"\n");
				
//				try
//				{
					for (ClassDeclaration c : getAllClasses())
					{
						VTableList vtables = c.getVTableNodes();
						
						writer.append(getWriter(vtables).generateSource(new StringBuilder()).append('\n'));
					}
//				}
//				catch (IOException e)
//				{
//					throw new RuntimeException(e);
//				}
			}, forceRecompile);
			
			forceRecompile = forceRecompile | FileUtils.writeIfDifferent(header, writer ->
			{
				writer.append("#ifndef NOVA_VTABLE_DECLARATIONS\n");
				writer.append("#define NOVA_VTABLE_DECLARATIONS\n\n");
				
//				try
//				{
					for (ClassDeclaration c : getAllClasses())
					{
						VTableList vtables = c.getVTableNodes();
						
						writer.append(getWriter(vtables.getExtensionVTable()).generateTypedef(new StringBuilder()).append('\n'));
						writer.append(getWriter(vtables.getExtensionVTable()).generateExternDeclaration(new StringBuilder()).append('\n'));
					}	
//				}
//				catch (IOException e)
//				{
//					throw new RuntimeException(e);
//				}
				
				writer.append("\n#include <Nova.h>\n");
				
				writer.append(getAllIncludes()).append('\n');
				
//				try
//				{
					for (ClassDeclaration c : getAllClasses())
					{
						VTableList vtables = c.getVTableNodes();
						
						writer.append(getWriter(vtables).generateHeader(new StringBuilder()).append('\n'));
	//					getWriter(vtables).generateSource(builder).append('\n');
					}
//				}
//				catch (IOException e)
//				{
//					throw new RuntimeException(e);
//				}
				
				writer.append("#endif");
			}, forceRecompile);
			
			compileEngine.forceRecompile = forceRecompile;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Insert the main method into the correct file. Also set up the
	 * initialization for the program within the main method.
	 */
	public void insertMainMethod()
	{
		MethodDeclaration mainMethod = tree.getMainMethod(mainClass);
		
		if (mainMethod == null)
		{
			if (!controller.isFlagEnabled(LIBRARY))
			{
				if (mainClass != null)
				{
					SyntaxMessage.error("No main method found in class '" + mainClass + "'", controller);
				}
				else
				{
					SyntaxMessage.error("No main method found in program", controller);
				}
				
				controller.completed(true);
			}
			
			return;
		}
		
		StringBuilder staticBlockCalls  = generateStaticBlockCalls();
		StringBuilder nativeAssignments = generateNativeVirtualMethodAssignments();
		StringBuilder vtableClassInstanceAssignments = generateVTableClassInstanceAssignments((NovaMethodDeclaration)mainMethod);
		StringBuilder vtableClassArray = generateVTableClassArray();
		StringBuilder vtableClassInstancePropertyAssignments = generateVTableClassInstancePropertyAssignments();
		
		FileDeclaration fileDeclaration = mainMethod.getFileDeclaration();
		
		if (mainMethod != null)
		{
//			FileDeclaration file = mainMethod.getFileDeclaration();
//			file.addChild(Import.decodeStatement(file, "import \"GC\"", file.getLocationIn(), true, false));
			Value gcInit = (Value)SyntaxTree.decodeIdentifierAccess(mainMethod, "GC.init()", mainMethod.getLocationIn(), true);
			Value gcColl = (Value)SyntaxTree.decodeIdentifierAccess(mainMethod, "GC.collect()", mainMethod.getLocationIn(), true);
			Value enter  = (Value)SyntaxTree.decodeIdentifierAccess(mainMethod, "Console.waitForEnter()", mainMethod.getLocationIn(), true);
			
			Instantiation nullConstructor = Instantiation.decodeStatement(mainMethod, "new Null()", mainMethod.getLocationIn(), true);
			
			NativeAnnotation annotation = NativeAnnotation.decodeStatement(mainMethod, "Native", "", mainMethod.getLocationIn(), true);
			annotation.onAfterDecoded();
			Constructor   strConstructor  = (Constructor)((MethodCall)Instantiation.decodeStatement(mainMethod, "new String(new Char[0])", mainMethod.getLocationIn(), true).getIdentifier()).getDeclaration();
			strConstructor.addAnnotation(annotation);
			
			File header = new File(controller.outputDirectory, MAIN_FUNCTION_FILE_NAME + ".h");
			File source = new File(controller.outputDirectory, MAIN_FUNCTION_FILE_NAME + ".c");
			
			try
			{
				FileUtils.writeIfDifferent(header, writer ->
				{
					writer.write("#ifndef NOVA_MAIN_FUNCTION_HEADER\n");
					writer.write("#define NOVA_MAIN_FUNCTION_HEADER\n\n");
					
					writer.write("#include <Nova.h>\n");
					writer.write("#include <InterfaceVTable.h>\n");
					writer.write("#include <ExceptionHandler.h>\n");
					
					writer.write("#include <");
					writer.write(getWriter(fileDeclaration).generateHeaderName());
					writer.write(">\n");
					
					writer.write("\n#endif");
				});
				
				FileUtils.writeIfDifferent(source, writer ->
				{
					writer.append("#include \"").append(MAIN_FUNCTION_FILE_NAME).append(".h\"").append('\n').append('\n');
					
					writer.append("nova_primitive_Nova_Null* nova_null;").append('\n');
					writer.append("void* ").append(Literal.GARBAGE_IDENTIFIER).append(';').append('\n');
					writer.append("typedef void (*thread_join_function_type)(void*, nova_exception_Nova_ExceptionData*, nova_Nova_Object*, int, nova_datastruct_list_Nova_Array*, void*);\n");
					writer.append("void novaJoinActiveThreads(void* this, nova_exception_Nova_ExceptionData* exceptionData, nova_thread_Nova_Thread* _1, int _2, void* _3, void* context)\n");
					writer.append("{\n");
					writer.append("nova_thread_Nova_Thread_Nova_join(_1, exceptionData);\n");
					writer.append("}\n");
					writer.append('\n');
					writer.append("int main(int argc, char** argvs)").append('\n');
					writer.append("{").append('\n');
					writer.append("#ifdef _WIN32\nsetProgramName(argvs[0]);").append('\n');
					writer.append("//signal(SIGSEGV, nova_signal_handler);").append('\n');
					writer.append("SetUnhandledExceptionFilter(nova_exception_handler);\n#endif").append('\n');
					writer.append("nova_Nova_String** args;").append('\n');
					writer.append("int      i;").append('\n').append('\n');
					writer.append("nova_exception_Nova_ExceptionData* ").append(Exception.EXCEPTION_DATA_IDENTIFIER).append(" = 0;").append('\n');
					//			writer.append	("ShowWindow(FindWindowA(\"ConsoleWindowClass\", NULL), 0);").append('\n');
					//			writer.append	("FreeConsole();").append('\n');
					//			writer.append	("AllocConsole();").append('\n');
					writer.append("srand(currentTimeMillis());").append('\n');
					writer.append(Literal.GARBAGE_IDENTIFIER).append(" = malloc(sizeof(void*));").append('\n');
					writer.append(getWriter(gcInit).generateSource()).append('\n');
					writer.append("nova_null = ").append(getWriter(nullConstructor).generateSourceFragment()).append(';').append('\n');
					writer.append(nativeAssignments).append('\n');
					writer.append(vtableClassInstanceAssignments).append('\n');
					writer.append(vtableClassInstancePropertyAssignments).append('\n');
					writer.append(vtableClassArray).append('\n');
					writer.append(staticBlockCalls).append('\n');
					writer.append("args = (nova_Nova_String**)NOVA_MALLOC(argc * sizeof(nova_Nova_String));").append('\n');
					writer.append('\n');
					writer.append("for (i = 0; i < argc; i++)").append('\n');
					writer.append("{").append('\n');
					writer.append("char* str = (char*)NOVA_MALLOC(sizeof(char) * strlen(argvs[i]) + 1);").append('\n');
					writer.append("copy_string(str, argvs[i]);").append('\n');
					writer.append("args[i] = ").append(getWriter(strConstructor).generateSourceName()).append("(0, 0, str);").append('\n');
					writer.append("}").append('\n');
					writer.append("nova_datastruct_list_Nova_Array* argsArray = nova_datastruct_list_Nova_Array_2_Nova_construct(0, exceptionData, (nova_Nova_Object**)args, argc);");
					writer.append('\n');
					writer.append("TRY").append('\n');
					writer.append('{').append('\n');
					writer.append(getWriter(mainMethod).generateSourceName()).append("(0, ").append(Exception.EXCEPTION_DATA_IDENTIFIER).append(", argsArray);").append('\n');
					writer.append('}').append('\n');
					writer.append("CATCH (").append(getWriter(tree.getRoot().getProgram().getClassDeclaration("nova/exception/Exception")).getVTableClassInstance()).append(')').append('\n');
					writer.append('{').append('\n');
					writer.append("char* message = \"Exception in Thread 'main'\";").append('\n');
					writer.append("nova_exception_Nova_Exception* base = (nova_exception_Nova_Exception*)").append(Exception.EXCEPTION_DATA_IDENTIFIER).append("->nova_exception_Nova_ExceptionData_Nova_thrownException;").append('\n');
					writer.append("if (base != 0 && base->nova_exception_Nova_Exception_Nova_message != 0 && base->nova_exception_Nova_Exception_Nova_message != (nova_Nova_String*)nova_null) {").append('\n');
					writer.append("printf(\"%s: %s\", message, base->nova_exception_Nova_Exception_Nova_message->nova_Nova_String_Nova_chars->nova_datastruct_list_Nova_Array_Nova_data);").append('\n');
					writer.append("} else {").append('\n');
					writer.append("puts(message);").append('\n');
					writer.append("}").append('\n');
					//			writer.append		(getWriter(enter).generateSource()).append('\n');
					writer.append('}').append('\n');
					writer.append("FINALLY").append('\n');
					writer.append('{').append('\n');
					writer.append('\n');
					writer.append('}').append('\n');
					writer.append("END_TRY;").append('\n');
					writer.append("nova_datastruct_list_Nova_ImmutableArray_Nova_forEach((nova_datastruct_list_Nova_ImmutableArray*)nova_thread_Nova_Thread_Nova_ACTIVE_THREADS, exceptionData, (thread_join_function_type)&novaJoinActiveThreads, 0, 0);\n");
					
					if (OS == WINDOWS)
					{
						writer.append("FreeConsole();").append('\n');
					}
					
					writer.append("NOVA_FREE(args);").append('\n');
					writer.append(getWriter(gcColl).generateSource()).append('\n');
					writer.append('\n');
					writer.append("return 0;").append('\n');
					writer.append("}\n");
				});
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	private StringBuilder generateStaticBlockCalls()
	{
		StringBuilder builder = new StringBuilder();
		
		Program root = tree.getRoot();
		
		for (int i = 0; i < root.getNumVisibleChildren(); i++)
		{
			FileDeclaration  file  = root.getVisibleChild(i);
			
			for (ClassDeclaration clazz : file.getClassDeclarations())
			{
				TypeList<StaticBlock> blocks = clazz.getStaticBlockList();
				
				for (int j = 0; j < blocks.getNumVisibleChildren(); j++)
				{
					StaticBlockWriter.generateMethodCall(builder, clazz).append(';').append('\n');
				}
			}
		}
		
		return builder;
	}
}
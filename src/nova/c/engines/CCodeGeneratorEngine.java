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
import java.util.ArrayList;
import java.util.HashSet;

import static net.fathomsoft.nova.Nova.*;
import static nova.c.nodewriters.NodeWriter.getWriter;

public class CCodeGeneratorEngine extends CodeGeneratorEngine
{
	private ArrayList<File>		cSourceFiles, cHeaderFiles;
	
	public File					nativeInterfaceSource, nativeInterfaceHeader;
	public File					interfaceVTableHeader;
	
	private static final String NATIVE_INTERFACE_FILE_NAME = "NovaNativeInterface";
	private static final String INTERFACE_VTABLE_FILE_NAME = "InterfaceVTable";
	private static final String ENVIRONMENT_VAR            = "novaEnv";
	
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
		generateNativeInterface();
		generateInterfaceVTable();
		
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
					File headerFile = FileUtils.writeFile(getWriter(file).generateHeaderName(), outputDir, header);
					File sourceFile = FileUtils.writeFile(getWriter(file).generateSourceName(), outputDir, source);
					
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
	
	public boolean writeClassData()
	{
		try
		{
			PrintWriter writer = FileUtils.getFileWriter("NovaClassData.h", controller.outputDirectory);
			
			writer.print("#ifndef NOVA_CLASS_DATA\n#define NOVA_CLASS_DATA\n\n");
			
			ClassDeclaration clazz = controller.getTree().getRoot().getClassDeclaration("nova/Class");
			
			writer.print("typedef struct NovaClassData NovaClassData;\n\n");
			
			Interface[] interfaces = getAllInterfaces();
			
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
			
			writer.print("};\n");
			
			writer.print("\n#endif");
			writer.close();
			
			writer = FileUtils.getFileWriter("NovaClassData.c", controller.outputDirectory);
			
			writer.write("#include <NovaClassData.h>\n\n");
			
			for (Interface i : interfaces)
			{
				getWriter(i).writeDefaultVTable(writer);
			}
			
			writer.close();
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
		StringBuilder nativeInterface = new StringBuilder();
		
		nativeInterface.append("#ifndef NOVA_NATIVE_INTERFACE\n");
		nativeInterface.append("#define NOVA_NATIVE_INTERFACE\n\n");
		
		nativeInterface.append(getAllIncludes());
		
		nativeInterface.append('\n');
		
		for (FileDeclaration file : tree.getFiles())
		{
			getWriter(file).generateHeaderNativeInterface(nativeInterface).append("\n");
		}
		
		nativeInterface.append("\ntypedef struct nova_env\n");
		nativeInterface.append("{\n");
		
		for (FileDeclaration file : tree.getFiles())
		{
			for (ClassDeclaration clazz : file.getClassDeclarations())
			{
				getWriter(clazz).generateSourceName(nativeInterface, "native").append(" ").append(clazz.getNativeLocation()).append(";\n");
			}
		}
		
		nativeInterface.append("} nova_env;\n\n");
		nativeInterface.append("extern nova_env " + ENVIRONMENT_VAR + ";\n\n");
		nativeInterface.append("\n#endif\n");
		
		try
		{
			File include = new File(StringUtils.removeSurroundingQuotes(getIncludeDir()));
			
			controller.outputDirectory.mkdirs();
			
			nativeInterfaceHeader = FileUtils.writeFile(NATIVE_INTERFACE_FILE_NAME + ".h", controller.outputDirectory, nativeInterface.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void generateNativeInterfaceSource()
	{
		StringBuilder nativeInterface = new StringBuilder();
		
		nativeInterface.append("#include <precompiled.h>\n");
		nativeInterface.append("#include \"" + NATIVE_INTERFACE_FILE_NAME + ".h\"\n\n");
		
		nativeInterface.append("nova_env " + ENVIRONMENT_VAR + " = {\n");
		
		for (FileDeclaration file : tree.getFiles())
		{
			getWriter(file).generateSourceNativeInterface(nativeInterface).append('\n');
		}
		
		nativeInterface.append("};\n");
		
		try
		{
			nativeInterfaceSource = FileUtils.writeFile(NATIVE_INTERFACE_FILE_NAME + ".c", controller.outputDirectory, nativeInterface.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void generateInterfaceVTable()
	{
		String header = generateInterfaceVTableHeader();
		
		try
		{
			interfaceVTableHeader = FileUtils.writeFile(INTERFACE_VTABLE_FILE_NAME + ".h", controller.outputDirectory, header);
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
		
		ClassDeclaration classClass = tree.getRoot().getClassDeclaration("nova/Class");
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
			
			StringBuilder mainMethodText = new StringBuilder();
			
			mainMethodText.append('\n').append('\n');
			mainMethodText.append("nova_primitive_Nova_Null* nova_null;").append('\n');
			mainMethodText.append("void* ").append(Literal.GARBAGE_IDENTIFIER).append(';').append('\n');
			mainMethodText.append('\n');
			mainMethodText.append("int main(int argc, char** argvs)").append('\n');
			mainMethodText.append("{").append('\n');
			mainMethodText.append	("#ifdef _WIN32\nsetProgramName(argvs[0]);").append('\n');
			mainMethodText.append	("//signal(SIGSEGV, nova_signal_handler);").append('\n');
			mainMethodText.append	("SetUnhandledExceptionFilter(nova_exception_handler);\n#endif").append('\n');
			mainMethodText.append	("nova_Nova_String** args;").append('\n');
			mainMethodText.append	("int      i;").append('\n').append('\n');
			mainMethodText.append	("nova_exception_Nova_ExceptionData* ").append(Exception.EXCEPTION_DATA_IDENTIFIER).append(" = 0;").append('\n');
//			mainMethodText.append	("ShowWindow(FindWindowA(\"ConsoleWindowClass\", NULL), 0);").append('\n');
//			mainMethodText.append	("FreeConsole();").append('\n');
//			mainMethodText.append	("AllocConsole();").append('\n');
			mainMethodText.append	("srand(currentTimeMillis());").append('\n');
			mainMethodText.append	(Literal.GARBAGE_IDENTIFIER).append(" = malloc(sizeof(void*));").append('\n');
			mainMethodText.append	(getWriter(gcInit).generateSource()).append('\n');
			mainMethodText.append	("nova_null = ").append(getWriter(nullConstructor).generateSourceFragment()).append(';').append('\n');
			mainMethodText.append	(nativeAssignments).append('\n');
			mainMethodText.append	(vtableClassInstanceAssignments).append('\n');
			mainMethodText.append	(vtableClassInstancePropertyAssignments).append('\n');
			mainMethodText.append	(vtableClassArray).append('\n');
			mainMethodText.append	(staticBlockCalls).append('\n');
			mainMethodText.append	("args = (nova_Nova_String**)NOVA_MALLOC(argc * sizeof(nova_Nova_String));").append('\n');
			mainMethodText.append	('\n');
			mainMethodText.append	("for (i = 0; i < argc; i++)").append('\n');
			mainMethodText.append	("{").append('\n');
			mainMethodText.append		("char* str = (char*)NOVA_MALLOC(sizeof(char) * strlen(argvs[i]) + 1);").append('\n');
			mainMethodText.append		("copy_string(str, argvs[i]);").append('\n');
			mainMethodText.append		("args[i] = ").append(getWriter(strConstructor).generateSourceName()).append("(0, 0, str);").append('\n');
			mainMethodText.append	("}").append('\n');
			mainMethodText.append	("nova_datastruct_list_Nova_Array* argsArray = nova_datastruct_list_Nova_Array_2_Nova_construct(0, exceptionData, (nova_Nova_Object**)args, argc);");
			mainMethodText.append	('\n');
			mainMethodText.append	("TRY").append('\n');
			mainMethodText.append	('{').append('\n');
			mainMethodText.append		(getWriter(mainMethod).generateSourceName()).append("(0, ").append(Exception.EXCEPTION_DATA_IDENTIFIER).append(", argsArray);").append('\n');
			mainMethodText.append	('}').append('\n');
			mainMethodText.append	("CATCH (").append(getWriter(tree.getRoot().getProgram().getClassDeclaration("nova/exception/Exception")).getVTableClassInstance()).append(')').append('\n');
			mainMethodText.append	('{').append('\n');
			mainMethodText.append		("char* message = \"Exception in Thread 'main'\";").append('\n');
			mainMethodText.append		("nova_exception_Nova_Exception* base = (nova_exception_Nova_Exception*)").append(Exception.EXCEPTION_DATA_IDENTIFIER).append("->nova_exception_Nova_ExceptionData_Nova_thrownException;").append('\n');
			mainMethodText.append		("if (base != 0 && base->nova_exception_Nova_Exception_Nova_message != 0 && base->nova_exception_Nova_Exception_Nova_message != (nova_Nova_String*)nova_null) {").append('\n');
			mainMethodText.append			("printf(\"%s: %s\", message, base->nova_exception_Nova_Exception_Nova_message->nova_Nova_String_Nova_chars->nova_datastruct_list_Nova_Array_Nova_data);").append('\n');
			mainMethodText.append		("} else {").append('\n');
			mainMethodText.append			("puts(message);").append('\n');
			mainMethodText.append		("}").append('\n');
			mainMethodText.append		(getWriter(enter).generateSource()).append('\n');
			mainMethodText.append	('}').append('\n');
			mainMethodText.append	("FINALLY").append('\n');
			mainMethodText.append	('{').append('\n');
			mainMethodText.append		('\n');
			mainMethodText.append	('}').append('\n');
			mainMethodText.append	("END_TRY;").append('\n');
			
			if (OS == WINDOWS)
			{
				mainMethodText.append("FreeConsole();").append('\n');
			}
			
			mainMethodText.append   ("NOVA_FREE(args);").append('\n');
			mainMethodText.append	(getWriter(gcColl).generateSource()).append('\n');
			mainMethodText.append	('\n');
			mainMethodText.append	("return 0;").append('\n');
			mainMethodText.append("}\n");
			
			String newSource = getWriter(fileDeclaration).generateSource() + mainMethodText.toString();
			
			newSource = SyntaxUtils.formatText(newSource);
			
			fileDeclaration.setSource(newSource);
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
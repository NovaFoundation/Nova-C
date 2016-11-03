package nova.c.nodewriters;

import net.fathomsoft.nova.Nova;
import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.tree.Package;
import net.fathomsoft.nova.tree.exceptionhandling.Exception;
import net.fathomsoft.nova.tree.variables.FieldList;
import net.fathomsoft.nova.tree.variables.InstanceFieldList;
import net.fathomsoft.nova.util.Location;

import java.io.PrintWriter;

public abstract class ClassDeclarationWriter extends InstanceDeclarationWriter
{
	public abstract ClassDeclaration node();
	
	/**
	 * Generate the C output for when node() value node is being used
	 * as an argument for a method call.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @param callingMethod The method that is being called by the
	 * 		specified Identifier.
	 * @return The C output for when node() value node is being used
	 * 		as an argument for a method call.
	 */
	public StringBuilder generateArgumentReference(StringBuilder builder, Identifier callingMethod)
	{
		if (callingMethod instanceof MethodCall)
		{
			CallableMethod declaration = ((MethodCall)callingMethod).getInferredDeclaration();
			
			if (declaration.isStatic() || declaration instanceof Constructor)
			{
				Parameter ref = declaration.getParameterList().getObjectReference();
				
				return getWriter(ref).generateNullOutput(builder);
			}
			else if (declaration instanceof ClosureDeclaration)
			{
				ClosureDeclaration closure = (ClosureDeclaration)declaration;
				
				return getWriter(closure).generateSourceName(builder, "ref");
			}
		}
		
		return super.generateArgumentReference(builder, callingMethod);
	}
	
	public StringBuilder generateHeaderNativeInterface(StringBuilder builder)
	{
		MethodDeclaration[] methods = node().getVisibleNativeMethods();
		
            /*if (methods.length <= 0)
            {
                return builder;
            }*/
		
		String name = generateSourceName("native").toString();
		
		for (MethodDeclaration method : methods)
		{
			builder.append("typedef " + getWriter(method).generateType() + " (*");
			
			getWriter(method).generateSourceNativeName(builder, true).append(")(");
			
			ParameterList params = method.getParameterList();
			
			getWriter(params).generateHeader(builder).append(");\n");
		}
		
		builder.append("\ntypedef struct " + name + "\n");
		builder.append("{\n");
		
		for (MethodDeclaration method : methods)
		{
			if (method.isInstance())
			{
				getWriter(method).generateSourceNativeName(builder, true).append(" ");
				getWriter(method).generateSourceNativeName(builder, false).append(";\n");
			}
		}
		
		builder.append("} " + name + ";\n");
		
		return builder;
	}
	
	public StringBuilder generateSourceNativeInterface(StringBuilder builder)
	{
		//		String name = generateSourceName("native").toString();
		
		MethodDeclaration[] methods = node().getVisibleNativeMethods();
		
		//		builder.append('\n');
		
		//		builder.append("struct " + name + "\n");
		builder.append("{\n");
		
		for (MethodDeclaration method : methods)
		{
			if (method.isInstance())
			{
				String value = "&" + getWriter(method).generateSourceName();
				
				if (method instanceof NovaMethodDeclaration)
				{
					NovaMethodDeclaration n = (NovaMethodDeclaration) method;
					
					if (n.isOverridden() && !(n instanceof Constructor))
					{
						value = "0";//getVTableNode().getName() + "." + n.generateVirtualMethodName();
					}
				}
				
				builder.append(value + ",\n");
			}
		}
		
		builder.append("},\n");
		
		return builder;
	}
	
	public StringBuilder writeClassDataDeclaration(StringBuilder builder)
	{
		return builder.append("NovaClassData classData;\n");
	}
	
	public StringBuilder writeClassData(StringBuilder builder)
	{
		
		
		return builder;
	}
	
	public PrintWriter writeClassInstanceDeclaration(PrintWriter writer)
	{
		ClassDeclarationWriter clazz = getWriter(node().getProgram().getClassDeclaration("nova/Class"));
		
		writer.print("extern " + clazz.generateType() + " " + getClassInstanceName() + ";\n");
		
		return writer;
	}
	
	public String getClassInstanceName()
	{
		return generateSourceName(getClassInstanceVTableName()).toString();
	}
	
	public String getVTableClassInstance()
	{
		return getWriter(node().getVTableNodes().getExtensionVTable()).generateSourceName() + "." + getClassInstanceVTableName();
	}
	
	public StringBuilder generateVTableClassInstanceAssignment(StringBuilder builder, NovaMethodDeclaration method)
	{
		ExtensionVTableWriter vtable = getWriter(node().getVTableNodes().getExtensionVTable());
		
		MethodDeclaration constructor = node().getProgram().getClassDeclaration("nova/Class").getConstructorList().getChild(0);
		
		//Assignment a = Assignment.generateDefault(method, Location.INVALID);
		
		MethodCall call = MethodCall.decodeStatement(method, "Class(\"" + node().getClassLocation() + "\", " + (node() instanceof Interface ? "true" : "false") + ")", Location.INVALID, true, false, constructor);
		
		//Literal classData = new Literal(method, Location.INVALID);
		//classData.value = "";
		
		//a.getAssignee().toValue().replaceWith(classData);
		//a.getAssignmentNode().replaceWith(call);
		
		MethodCallWriter callWriter = getWriter(call);
		
		builder.append(getVTableClassInstance() + " = " + callWriter.generateSourceFragment() + ";\n");
		
		return builder;
	}
	
	public StringBuilder generateVTableClassPropertyAssignments(StringBuilder builder)
	{
		generateVTableExtensionAssignment(builder);
		generateVTableInterfaceAssignments(builder);
		
		return builder;
	}
	
	public StringBuilder generateVTableExtensionAssignment(StringBuilder builder)
	{
		if (node().doesExtendClass())
		{
			ClassDeclaration clazz = node().getProgram().getClassDeclaration("nova/Class");
			
			builder.append(getVTableClassInstance()).append("->").append(getWriter(clazz.getField("extension")).generateSourceName()).append(" = ")
				.append(getWriter(node().getExtendedClassDeclaration()).getVTableClassInstance()).append(";\n");
		}
		
		return builder;
	}
	
	public StringBuilder generateVTableInterfaceAssignments(StringBuilder builder)
	{
		ClassDeclaration clazz = node().getProgram().getClassDeclaration("nova/Class");
		ClassDeclaration array = node().getProgram().getClassDeclaration("nova/datastruct/list/Array");
		
		String interfaces = getVTableClassInstance() + "->" + getWriter(clazz.getField("interfaces")).generateSourceName();
		
		NovaMethodDeclaration add = (NovaMethodDeclaration)array.getMethods("add", 1)[0];
		String cast = getWriter(add.getParameterList().getParameter(0)).generateTypeCast().toString();
		
		for (Interface i : node().getImplementedInterfaces(false))
		{
			builder.append(getWriter(add).generateSourceName()).append("(").append(interfaces).append(", ")
				.append(Exception.EXCEPTION_DATA_IDENTIFIER).append(", ").append(cast).append(getWriter(i).getVTableClassInstance()).append(");\n");
		}
		
		return builder;
	}
	
	public static String getClassInstanceVTableName()
	{
		return "classInstance";
	}
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		VTableList vtables = node().getVTableNodes();
		
		getWriter(vtables).generateHeader(builder).append('\n');
		
		builder.append("CCLASS_CLASS").append('\n').append('(').append('\n');
		
		generateSourceName(builder).append(", ").append('\n').append('\n');
		
		VTable extension = node().getVTableNodes().getExtensionVTable();
		
		builder.append(getWriter(extension).generateType()).append("* ").append(VTable.IDENTIFIER).append(";\n");
		
		//writeClassDataDeclaration(builder);
		
		FieldList list = node().getFieldList();
		
		getWriter(list).generateNonStaticHeader(builder);
		
		if (node().containsNonStaticPrivateData())
		{
			builder.append("struct Private* prv;").append('\n');
		}
		
		builder.append(')').append('\n');
		
		FieldList fields = node().getFieldList();
		
		getWriter(fields).generateStaticHeader(builder).append('\n');
		
		if (node().getStaticBlockList().getNumVisibleChildren() > 0)
		{
			StaticBlock child = node().getStaticBlockList().getChild(0);
			
			getWriter(child).generateHeader(builder, node());
		}
		
		MethodList constructors = node().getConstructorList();
		getWriter(constructors).generateHeader(builder);
		
		getWriter(node().getDestructorList()).generateHeader(builder);
		getWriter(node().getMethodList()).generateHeader(builder);
		getWriter(node().getPropertyMethodList()).generateHeader(builder);
		getWriter(node().getHiddenMethodList()).generateHeader(builder);
		getWriter(node().getVirtualMethodList()).generateHeader(builder);
		
		return builder;
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		VTableList vtables = node().getVTableNodes();
		
		getWriter(vtables).generateSource(builder).append('\n');
		
		if (node().containsNonStaticPrivateData())
		{
			builder.append("CCLASS_PRIVATE").append('\n').append('(').append('\n').append(generatePrivateFieldsSource()).append(')').append('\n');
		}
		
		builder.append(generatePrivateMethodPrototypes());
		
		FieldList fields = node().getFieldList();
		
		getWriter(fields).generateStaticSource(builder);
		
		for (int i = node().getNumDefaultChildren(); i < node().getNumChildren(); i++)
		{
			Node child = node().getChild(i);
			
			builder.append('\n').append(getWriter(child).generateSource());
		}
		
		fields = node().getFieldList();
		
		getWriter(fields).generateNonStaticSource(builder);
		
		generateStaticBlocksSource(builder);
		
		getWriter(node().getConstructorList()).generateSource(builder);
		getWriter(node().getDestructorList()).generateSource(builder);
		getWriter(node().getMethodList()).generateSource(builder);
		getWriter(node().getPropertyMethodList()).generateSource(builder);
		getWriter(node().getHiddenMethodList()).generateSource(builder);
		getWriter(node().getVirtualMethodList()).generateSource(builder);
		
		return builder;
	}
	
	private StringBuilder generateStaticBlocksSource(StringBuilder builder)
	{
		if (node().getStaticBlockList().getNumVisibleChildren() > 0)
		{
			StaticBlock block = node().getStaticBlockList().getChild(0);
			
			getWriter(block).generateMethodHeader(builder, node()).append('\n');
			
			builder.append('{').append('\n');
			
			for (int i = 0; i < node().getStaticBlockList().getNumVisibleChildren(); i++)
			{
				block = node().getStaticBlockList().getChild(i);
				
				getWriter(block).generateSource(builder);
			}
			
			builder.append('}').append('\n');
		}
		
		return builder;
	}
	
	/**
	 * Generate the C source representation of the private field
	 * declarations.
	 *
	 * @return The StringBuilder with the appended data.
	 */
	private StringBuilder generatePrivateFieldsSource()
	{
		return generatePrivateFieldsSource(new StringBuilder());
	}
	
	/**
	 * Generate the C source representation of the private field
	 * declarations.
	 *
	 * @param builder The StringBuilder to append that data to.
	 * @return The StringBuilder with the appended data.
	 */
	private StringBuilder generatePrivateFieldsSource(StringBuilder builder)
	{
		if (node().getExtendedClassDeclaration() != null)
		{
			ClassDeclaration clazz = node().getExtendedClassDeclaration();
			
			getWriter(clazz).generatePrivateFieldsSource(builder);
		}
		
		InstanceFieldList fields = node().getFieldList().getPrivateFieldList();
		
		return getWriter(fields).generateSource(builder);
	}
	
	public StringBuilder generateSourceName(StringBuilder builder, String uniquePrefix)
	{
		if (uniquePrefix == null)
		{
			uniquePrefix = Nova.LANGUAGE_NAME;
		}
		
		return generateUniquePrefix(builder).append(uniquePrefix).append("_").append(node().getName());
	}
	
	/**
	 * Generate the prototypes for specifically the private methods.
	 *
	 * @return A String containing the prototype definitions.
	 */
	private String generatePrivateMethodPrototypes()
	{
		StringBuilder  builder = new StringBuilder();
		
		generatePrototypes(builder, node().getMethodList());
		generatePrototypes(builder, node().getPropertyMethodList());
		
		if (builder.length() > 0)
		{
			builder.insert(0, '\n');
		}
		
		return builder.toString();
	}
	
	private void generatePrototypes(StringBuilder builder, MethodList methods)
	{
		for (int i = 0; i < methods.getNumChildren(); i++)
		{
			MethodDeclaration methodDeclaration = methods.getChild(i);
			
			if (methodDeclaration.getVisibility() == InstanceDeclaration.PRIVATE)
			{
				getWriter(methodDeclaration).generateSourcePrototype(builder).append('\n');
			}
		}
	}
	
	public StringBuilder generateUniquePrefix()
	{
		return generateUniquePrefix(new StringBuilder());
	}
	
	/**
	 * Get the prefix that is used for the data that is contained
	 * within the specified class.<br>
	 * <br>
	 * For example:
	 * <blockquote><pre>
	 * package "node()/is/my/package"
	 *
	 * public class Test
	 * {
	 * 	...
	 * }</pre></blockquote>
	 * The method prefix would look like:
	 * "<code>node()_is_my_package_NovaTest</code>"
	 *
	 * @return The prefix that is used for the data contained within
	 * 		the class.
	 */
	public StringBuilder generateUniquePrefix(StringBuilder builder)
	{
		Package p = node().getFileDeclaration().getPackage();
		
		return getWriter(p).generateLocation(builder).append('_');
	}
}
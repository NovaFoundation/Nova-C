package nova.c.nodewriters;

import net.fathomsoft.nova.error.SyntaxMessage;
import net.fathomsoft.nova.tree.*;
import net.fathomsoft.nova.util.Stack;

public abstract class ConstructorWriter extends BodyMethodDeclarationWriter
{
	public abstract Constructor node();
	
	public StringBuilder generateTypeName(StringBuilder builder)
	{
		return generateTypeClassName(builder);
	}
	
	public StringBuilder generateHeader(StringBuilder builder)
	{
		if (node().isVisibilityValid())
		{
			if (node().getVisibility() == InstanceDeclaration.PRIVATE)
			{
				return builder;
			}
		}
		
		if (node().isReference())
		{
			SyntaxMessage.error("Constructor cannot return a reference", node());
		}
		
		return generateSourcePrototype(builder).append('\n');
	}
	
	public StringBuilder generateSource(StringBuilder builder)
	{
		generateSourceSignature(builder).append('\n');
		
		builder.append('{').append('\n');
		
		ClassDeclaration classDeclaration = node().getParentClass();
		
		if (classDeclaration.containsNonStaticData() || classDeclaration.containsVirtualMethods())
		{
			ClassDeclaration clazz = node().getTypeClass();
			
			builder.append("CCLASS_NEW(").append(clazz.getTarget().generateSourceName()).append(", ").append(ParameterList.OBJECT_REFERENCE_IDENTIFIER);
			
			if (!classDeclaration.containsNonStaticPrivateData())
			{
				builder.append(",");
			}
			
			builder.append(");");
		}
		else
		{
			builder.append(ParameterList.OBJECT_REFERENCE_IDENTIFIER).append(" = ").append(generateTypeCast()).append("1").append(';');
		}
		
		builder.append('\n');
		
		VTable extension = node().getParentClass().getVTableNodes().getExtensionVTable();
		
		builder.append(ParameterList.OBJECT_REFERENCE_IDENTIFIER).append("->").append(VTable.IDENTIFIER).append(" = &").append(extension.getTarget().generateSourceName()).append(";\n");
		
		{
			Stack<AssignmentMethod> calls = new Stack<>();
			
			ClassDeclaration extended = node().getParentClass().getExtendedClassDeclaration();
			
			while (extended != null)
			{
				calls.push(extended.getAssignmentMethodNode());
				
				extended = extended.getExtendedClassDeclaration();
			}
			
			while (!calls.isEmpty())
			{
				AssignmentMethod method = calls.pop();
				
				if (method != null)
				{
					method.getTarget().generateMethodCall(builder, true);
				}
			}
		}
		
		// Generate super calls.
		{
			Stack<MethodCall> calls = new Stack<>();
			
			node().addSuperCallFor(calls, node());
			
			while (!calls.isEmpty())
			{
				MethodCall call = calls.pop();
				
				call.getTarget().generateSource(builder);
			}
		}
		
		AssignmentMethod assignmentMethod = node().getParentClass().getAssignmentMethodNode();
		
		assignmentMethod.getTarget().generateMethodCall(builder);
		
		builder.append('\n');
		
		Scope scope = node().getScope();
		
		scope.getTarget().generateSource(builder);
		
		builder.append('\n');
		
		builder.append("return ").append(ParameterList.OBJECT_REFERENCE_IDENTIFIER).append(';').append('\n');
		
		builder.append('}').append('\n');
		
		return builder;
	}
	
	public String getCName()
	{
		return Constructor.IDENTIFIER;
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.tree.variables.FieldList;

public abstract class FieldListWriter extends ListWriter
{
	public abstract FieldList node();
	
	/**
	 * Generate the C Header output for the all of the non-static
	 * variables.
	 *
	 * @return The C Header file output.
	 */
	public StringBuilder generateNonStaticHeader(StringBuilder builder)
	{
		return node().getPublicFieldList().getTarget().generateHeader(builder);
	}
	
	/**
	 * Generate the C Header output for the all of the public static
	 * variables.
	 *
	 * @return The C Header file output.
	 */
	public StringBuilder generateStaticHeader(StringBuilder builder)
	{
		return node().getPublicStaticFieldList().getTarget().generateHeader(builder);
	}
	
	/**
	 * Generate the C Source output for the all of the public static
	 * variables.
	 *
	 * @return The C Source file output.
	 */
	public StringBuilder generateStaticSource(StringBuilder builder)
	{
		return node().getPublicStaticFieldList().getTarget().generateSource(builder);
	}
	
	/**
	 * Generate the C Source output for the all of the non-static
	 * variables.
	 *
	 * @return The C Source file output.
	 */
	public StringBuilder generateNonStaticSource(StringBuilder builder)
	{
		return node().getPrivateStaticFieldList().getTarget().generateHeader(builder);
	}
}
package nova.c.nodewriters;

import net.fathomsoft.nova.error.UnimplementedOperationException;
import net.fathomsoft.nova.tree.*;

public abstract class NodeWriter
{
	public abstract Node node();
	
	/**
	 * Method that each Node overrides. Returns a String that translates
	 * the data that is stored in the Node to the C programming
	 * language header file syntax.
	 *
	 * @return The C header file syntax representation of the node().
	 */
	public final StringBuilder generateHeader()
	{
		return generateHeader(new StringBuilder());
	}
	
	/**
	 * Method that each Node can override. Returns a String that
	 * translates the data that is stored in the Node to the C
	 * programming language header file 'fragment' syntax.
	 *
	 * @return The C header syntax representation of the node().
	 */
	public final StringBuilder generateHeaderFragment()
	{
		return generateHeaderFragment(new StringBuilder());
	}
	
	/**
	 * Method that each Node overrides. Returns a String that translates
	 * the data that is stored in the Node to the C programming
	 * language source file syntax.
	 *
	 * @return The C source syntax representation of the node().
	 */
	public final StringBuilder generateSource()
	{
		return generateSource(new StringBuilder());
	}
	
	/**
	 * Method that each Node overrides. Returns a String that translates
	 * the data that is stored in the Node to the C programming
	 * language source file 'fragment' syntax.
	 *
	 * @return The C source syntax representation of the node().
	 */
	public final StringBuilder generateSourceFragment()
	{
		return generateSourceFragment(new StringBuilder());
	}
	
	/**
	 * Method that each Node overrides. Returns a String that translates
	 * the data that is stored in the Node to the C programming
	 * language header file syntax.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The C header file syntax representation of the node().
	 */
	public StringBuilder generateHeader(StringBuilder builder)
	{
		return generateHeaderFragment(builder).append('\n');
		//throw new UnimplementedOperationException("The C Header implementation for " + node().getClass().getName() + " has not been implemented yet.");
	}
	
	/**
	 * Method that each Node can override. Returns a String that
	 * translates the data that is stored in the Node to the C
	 * programming language header file 'fragment' syntax.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The C header syntax representation of the node().
	 */
	public StringBuilder generateHeaderFragment(StringBuilder builder)
	{
		throw new UnimplementedOperationException("The C Header fragment implementation for " + node().getClass().getName() + " has not been implemented yet.");
	}
	
	/**
	 * Method that each Node overrides. Returns a String that translates
	 * the data that is stored in the Node to the C programming
	 * language source file syntax.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The C source syntax representation of the node().
	 */
	public StringBuilder generateSource(StringBuilder builder)
	{
		return generateSourceFragment(builder).append('\n');
		//throw new UnimplementedOperationException("The C Source implementation for " + node().getClass().getName() + " has not been implemented yet.");
	}
	
	/**
	 * Method that each Node overrides. Returns a String that translates
	 * the data that is stored in the Node to the C programming
	 * language source file 'fragment' syntax.
	 *
	 * @param builder The StringBuilder to append the data to.
	 * @return The C source syntax representation of the node().
	 */
	public StringBuilder generateSourceFragment(StringBuilder builder)
	{
		throw new UnimplementedOperationException("The C Source fragment implementation for " + node().getClass().getName() + " has not been implemented yet.");
	}
}
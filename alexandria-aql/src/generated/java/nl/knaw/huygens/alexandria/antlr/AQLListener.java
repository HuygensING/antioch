// Generated from AQL.g4 by ANTLR 4.5.1

package nl.knaw.huygens.alexandria.antlr;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link AQLParser}.
 */
public interface AQLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link AQLParser#root}.
	 * @param ctx the parse tree
	 */
	void enterRoot(AQLParser.RootContext ctx);
	/**
	 * Exit a parse tree produced by {@link AQLParser#root}.
	 * @param ctx the parse tree
	 */
	void exitRoot(AQLParser.RootContext ctx);
	/**
	 * Enter a parse tree produced by {@link AQLParser#subquery}.
	 * @param ctx the parse tree
	 */
	void enterSubquery(AQLParser.SubqueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link AQLParser#subquery}.
	 * @param ctx the parse tree
	 */
	void exitSubquery(AQLParser.SubqueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link AQLParser#parameters}.
	 * @param ctx the parse tree
	 */
	void enterParameters(AQLParser.ParametersContext ctx);
	/**
	 * Exit a parse tree produced by {@link AQLParser#parameters}.
	 * @param ctx the parse tree
	 */
	void exitParameters(AQLParser.ParametersContext ctx);
	/**
	 * Enter a parse tree produced by {@link AQLParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(AQLParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link AQLParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(AQLParser.ParameterContext ctx);
}
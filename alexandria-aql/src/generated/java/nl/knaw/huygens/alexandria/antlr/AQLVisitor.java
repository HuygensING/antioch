// Generated from AQL.g4 by ANTLR 4.5.1

package nl.knaw.huygens.alexandria.antlr;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link AQLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface AQLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link AQLParser#root}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot(AQLParser.RootContext ctx);
	/**
	 * Visit a parse tree produced by {@link AQLParser#subquery}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubquery(AQLParser.SubqueryContext ctx);
	/**
	 * Visit a parse tree produced by {@link AQLParser#parameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameters(AQLParser.ParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link AQLParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(AQLParser.ParameterContext ctx);
}
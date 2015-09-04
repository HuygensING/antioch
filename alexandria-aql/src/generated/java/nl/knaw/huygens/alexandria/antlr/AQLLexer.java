// Generated from AQL.g4 by ANTLR 4.5.1

package nl.knaw.huygens.alexandria.antlr;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class AQLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, FIELDNAME=5, FUNCTION=6, STRINGPARAMETER=7, 
		LONGPARAMETER=8, WS=9;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "FIELDNAME", "FUNCTION", "STRINGPARAMETER", 
		"LONGPARAMETER", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "':'", "'('", "')'", "','"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, "FIELDNAME", "FUNCTION", "STRINGPARAMETER", 
		"LONGPARAMETER", "WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public AQLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "AQL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\13\u0096\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3"+
		"\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6l\n"+
		"\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\5\7\u0081\n\7\3\b\3\b\6\b\u0085\n\b\r\b\16\b\u0086\3\b\3\b\3"+
		"\t\6\t\u008c\n\t\r\t\16\t\u008d\3\n\6\n\u0091\n\n\r\n\16\n\u0092\3\n\3"+
		"\n\2\2\13\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\3\2\5\7\2\"\"/\60\62"+
		"=C\\c|\3\2\62;\5\2\13\f\17\17\"\"\u00a5\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2"+
		"\2\23\3\2\2\2\3\25\3\2\2\2\5\27\3\2\2\2\7\31\3\2\2\2\t\33\3\2\2\2\13k"+
		"\3\2\2\2\r\u0080\3\2\2\2\17\u0082\3\2\2\2\21\u008b\3\2\2\2\23\u0090\3"+
		"\2\2\2\25\26\7<\2\2\26\4\3\2\2\2\27\30\7*\2\2\30\6\3\2\2\2\31\32\7+\2"+
		"\2\32\b\3\2\2\2\33\34\7.\2\2\34\n\3\2\2\2\35\36\7k\2\2\36l\7f\2\2\37 "+
		"\7v\2\2 !\7{\2\2!\"\7r\2\2\"l\7g\2\2#$\7x\2\2$%\7c\2\2%&\7n\2\2&\'\7w"+
		"\2\2\'l\7g\2\2()\7t\2\2)*\7g\2\2*+\7u\2\2+,\7q\2\2,-\7w\2\2-.\7t\2\2."+
		"/\7e\2\2/\60\7g\2\2\60\61\7\60\2\2\61\62\7k\2\2\62l\7f\2\2\63\64\7u\2"+
		"\2\64\65\7w\2\2\65\66\7d\2\2\66\67\7t\2\2\678\7g\2\289\7u\2\29:\7q\2\2"+
		":;\7w\2\2;<\7t\2\2<=\7e\2\2=>\7g\2\2>?\7\60\2\2?@\7k\2\2@l\7f\2\2AB\7"+
		"t\2\2BC\7g\2\2CD\7u\2\2DE\7q\2\2EF\7w\2\2FG\7t\2\2GH\7e\2\2HI\7g\2\2I"+
		"J\7\60\2\2JK\7w\2\2KL\7t\2\2Ll\7n\2\2MN\7u\2\2NO\7w\2\2OP\7d\2\2PQ\7t"+
		"\2\2QR\7g\2\2RS\7u\2\2ST\7q\2\2TU\7w\2\2UV\7t\2\2VW\7e\2\2WX\7g\2\2XY"+
		"\7\60\2\2YZ\7w\2\2Z[\7t\2\2[l\7n\2\2\\]\7u\2\2]^\7v\2\2^_\7c\2\2_`\7v"+
		"\2\2`l\7g\2\2ab\7y\2\2bc\7j\2\2cl\7q\2\2de\7y\2\2ef\7j\2\2fg\7g\2\2gl"+
		"\7p\2\2hi\7y\2\2ij\7j\2\2jl\7{\2\2k\35\3\2\2\2k\37\3\2\2\2k#\3\2\2\2k"+
		"(\3\2\2\2k\63\3\2\2\2kA\3\2\2\2kM\3\2\2\2k\\\3\2\2\2ka\3\2\2\2kd\3\2\2"+
		"\2kh\3\2\2\2l\f\3\2\2\2mn\7g\2\2n\u0081\7s\2\2op\7o\2\2pq\7c\2\2qr\7v"+
		"\2\2rs\7e\2\2s\u0081\7j\2\2tu\7k\2\2uv\7p\2\2vw\7U\2\2wx\7g\2\2x\u0081"+
		"\7v\2\2yz\7k\2\2z{\7p\2\2{|\7T\2\2|}\7c\2\2}~\7p\2\2~\177\7i\2\2\177\u0081"+
		"\7g\2\2\u0080m\3\2\2\2\u0080o\3\2\2\2\u0080t\3\2\2\2\u0080y\3\2\2\2\u0081"+
		"\16\3\2\2\2\u0082\u0084\7$\2\2\u0083\u0085\t\2\2\2\u0084\u0083\3\2\2\2"+
		"\u0085\u0086\3\2\2\2\u0086\u0084\3\2\2\2\u0086\u0087\3\2\2\2\u0087\u0088"+
		"\3\2\2\2\u0088\u0089\7$\2\2\u0089\20\3\2\2\2\u008a\u008c\t\3\2\2\u008b"+
		"\u008a\3\2\2\2\u008c\u008d\3\2\2\2\u008d\u008b\3\2\2\2\u008d\u008e\3\2"+
		"\2\2\u008e\22\3\2\2\2\u008f\u0091\t\4\2\2\u0090\u008f\3\2\2\2\u0091\u0092"+
		"\3\2\2\2\u0092\u0090\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u0094\3\2\2\2\u0094"+
		"\u0095\b\n\2\2\u0095\24\3\2\2\2\b\2k\u0080\u0086\u008d\u0092\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
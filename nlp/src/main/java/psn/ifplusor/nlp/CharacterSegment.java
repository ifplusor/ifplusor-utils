package psn.ifplusor.nlp;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CharacterSegment {

	private static final ThreadLocal<IKSegmenter> threadSegmenter = new ThreadLocal<IKSegmenter>();

	public static String segment(String content) {

		Reader in = new StringReader(content);
		IKSegmenter _IKImplement = threadSegmenter.get();
		if (_IKImplement == null) {
			_IKImplement = new IKSegmenter(in, true);
			threadSegmenter.set(_IKImplement);
		} else {
			_IKImplement.reset(in);
		}

		String keywords = "";

		try {
			Lexeme nextLexeme = null;
			while ((nextLexeme = _IKImplement.next()) != null) {
				keywords = keywords + nextLexeme.getLexemeText() + "#";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return keywords;
	}
}

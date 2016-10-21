package psn.ifplusor.nlp;

import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class CharacterSegment {
	
	public static String segment(String content) {
		
		String keywords = "";
		
		Reader in = new StringReader(content);
		IKSegmenter _IKImplement = new IKSegmenter(in , true);
		
		Lexeme nextLexeme = null;
		try {
			while ((nextLexeme = _IKImplement.next()) != null) {
				keywords = keywords + nextLexeme.getLexemeText() + "#";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return keywords;
	}
}

package de.ws1718.ismla.JapaneseHelper.server;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.atilika.kuromoji.ipadic.Tokenizer;

import de.ws1718.ismla.JapaneseHelper.shared.InflectableToken;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;
import de.ws1718.ismla.JapaneseHelper.server.LookupServiceImpl;
import static de.ws1718.ismla.JapaneseHelper.shared.Inflection.*;

public class LookupServiceImplTest {
	Token tok1 = new Token("命", "いのち", "N", "1) life 2) lifespan 3) lifetime "
			+ "4) fate, destiny 5) the most important part or aspect of a thing [figuratively]");
	Token tok2 = new Token("命", "みこと", "N", "1) the words or pronouncements of a god or an emperor");
	Token tok3 = new Token("命", "みこと", "PRN", "1) you 2) that person");
	Token tok4 = new Token("和", "わ", "N", "1) peace, harmony 2) sum [arithmetic]");

	Token tok5 = new Token("来る", "くる", "VI3[kuru]", "1) To come (toward the speakerwriter)");
	InflectableToken tok6 = new InflectableToken(tok5);
	Token tok7 = new InflectedToken(tok6, "来る", "くる", TERMINAL);

	private void testPermutations(List<Token> expected, com.atilika.kuromoji.ipadic.Token tokKuromoji, Token tok1,
			Token tok2, Token tok3) {
		String pos = tokKuromoji.getPartOfSpeechLevel1();
		String pron = tokKuromoji.getPronunciation();
		String form = tokKuromoji.getSurface();
		assertEquals(expected, LookupServiceImpl.sortTokens(form, pos, pron, Arrays.asList(tok1, tok2, tok3)));
		assertEquals(expected, LookupServiceImpl.sortTokens(form, pos, pron, Arrays.asList(tok1, tok3, tok2)));
		assertEquals(expected, LookupServiceImpl.sortTokens(form, pos, pron, Arrays.asList(tok2, tok1, tok3)));
		assertEquals(expected, LookupServiceImpl.sortTokens(form, pos, pron, Arrays.asList(tok2, tok3, tok1)));
		assertEquals(expected, LookupServiceImpl.sortTokens(form, pos, pron, Arrays.asList(tok3, tok2, tok1)));
		assertEquals(expected, LookupServiceImpl.sortTokens(form, pos, pron, Arrays.asList(tok3, tok1, tok2)));
	}

	@Test
	public void testSortTokens() {
		// reading: イノチ = いのち; pos: 名詞 = N
		com.atilika.kuromoji.ipadic.Token tokKuromoji = new Tokenizer().tokenize("命").get(0);

		List<Token> expected = Arrays.asList(tok1, tok2, tok3);
		testPermutations(expected, tokKuromoji, tok1, tok2, tok3);
	}

	@Test
	public void testSortTokens2() {
		// reading: イノチ = いのち; pos: 名詞 = N
		com.atilika.kuromoji.ipadic.Token tokKuromoji = new Tokenizer().tokenize("命").get(0);

		List<Token> expected = Arrays.asList(tok1, tok4, tok3);
		testPermutations(expected, tokKuromoji, tok1, tok4, tok3);
	}

	@Test
	public void testSortTokensInflected() {
		com.atilika.kuromoji.ipadic.Token tokKuromoji = new Tokenizer().tokenize("来る").get(0);
		List<Token> expected = Arrays.asList(tok7, tok6, tok5);
		try {
			testPermutations(expected, tokKuromoji, tok5, tok6, tok7);
		} catch (AssertionError e) {
			expected = Arrays.asList(tok7, tok5, tok6);
			testPermutations(expected, tokKuromoji, tok5, tok6, tok7);
		}
	}

}

package de.ws1718.ismla.JapaneseHelper.preprocessing;

import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.ATTRIBUTIVE;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.ATTRIBUTIVE2;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.CAUSATIVE;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.CONJUNCTIVE;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.FORMAL;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.FORMAL_NEGATIVE;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.IMPERATIVE;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.IMPERFECTIVE;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.IMPERFECTIVE2;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.INFORMAL_PAST;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.NEGATIVE_CONTINUATIVE;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.PASSIVE;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.POTENTIAL;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.PROVISIONAL;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.TERMINAL;
import static de.ws1718.ismla.JapaneseHelper.preprocessing.Inflection.VOLITIONAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Multimap;

import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class InflectionTest {

	private static final String DICT_FILE = "src/main/webapp/WEB-INF/dictionary-full/dictionary-full.ser";
	private static Map<String, List<InflectedToken>> map = new HashMap<String, List<InflectedToken>>();
	private static final Logger logger = Logger.getLogger(InflectionTest.class.getSimpleName());

	// templates taken from Wiktionary (ADJ)
	private static final String I_TOK = "明るい";
	private static final String NA_TOK = "妙";
	// templates taken from Wiktionary (V)
	// all of the godan-type verbs (go-...) are inflected very similarly
	private static final String GO_BU_TOK = "飛ぶ";
	private static final String GO_RU_TOK = "伝わる";
	private static final String HONORIFIC_TOK = "下さる";
	private static final String ICHI_TOK = "生じる";
	private static final String SURU_I_KU_TOK = "対する";
	private static final String SURU_TSU_TOK = "察する";
	private static final String SURU_TOK = "会する";
	private static final String ZURU_TOK = "信ずる";
	// manually crafted templates
	private static final String KURU_TOK = "連れて来る";
	private static final String ARU_TOK = "ある"; // verbconj
	private static final String BESHI_TOK = "べし"; // verbconj
	private static final String KURERU_TOK = "くれる"; // verbconj
	private static final String SURU_INDEP_TOK = "為る"; // verbconj
	private static final String TARI_TOK = "惨憺";
	// other special cases
	private static final String QUESTION_MARK_I_TOK = "美しい";
	private static final String QUESTION_MARK_NA_TOK = "様";

	@BeforeClass
	public static void readTokens() {
		List<String> keys = new ArrayList<String>(Arrays.asList(I_TOK, NA_TOK, GO_BU_TOK, GO_RU_TOK, HONORIFIC_TOK,
				ICHI_TOK, SURU_I_KU_TOK, SURU_TSU_TOK, SURU_TOK, ZURU_TOK, KURU_TOK, ARU_TOK, BESHI_TOK, KURERU_TOK,
				SURU_INDEP_TOK, TARI_TOK, QUESTION_MARK_I_TOK, QUESTION_MARK_NA_TOK));

		logger.info("reading file");
		Multimap<String, Token> tokenMap = null;
		try (InputStream is = new FileInputStream(DICT_FILE); ObjectInputStream ois = new ObjectInputStream(is)) {
			tokenMap = (Multimap<String, Token>) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail();
		}

		logger.info("updating map");
		for (Token tok : tokenMap.values()) {
			if (tok instanceof InflectedToken) {
				InflectedToken infTok = (InflectedToken) tok;
				String lemma = infTok.getLemma();
				if (keys.contains(lemma)){
					List<InflectedToken> tokens = map.get(lemma);
					if (tokens == null) {
						tokens = new ArrayList<>();
					}
					tokens.add(infTok);
					map.put(lemma, tokens);
				}
			}
		}
		logger.info("done");

	}

	private void testInflection(List<InflectedToken> tokens, String pos, String translation, String form,
			String pronunciation, Inflection inflection, String lemma) {
		assertTrue(tokens
				.contains(new InflectedToken(form, pronunciation, pos, translation, inflection.toString(), lemma)));
	}

	@Test
	public void testI() {
		String lemma = I_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "A[i]";
		String translation = "1) bright, light, luminous 2) merry, cheerful";
		testInflection(tokens, pos, translation, "明るかろ", "あかるかろ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "明るかれ", "あかるかれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "明るかった", "あかるかった", INFORMAL_PAST, lemma);
		testInflection(tokens, pos, translation, "明るくないです", "あかるくないです", FORMAL_NEGATIVE, lemma);
		testInflection(tokens, pos, translation, "明るかったら", "あかるかったら", PROVISIONAL, lemma);
	}

	@Test
	public void testNa() {
		String lemma = NA_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		String pos = "A[na]";
		String translation = "1) strange 2) excellent";

		testInflection(tokens, pos, translation, "妙だろ", "みょうだろ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "妙であれ", "みょうであれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "妙だった", "みょうだった", INFORMAL_PAST, lemma);
		testInflection(tokens, pos, translation, "妙ではありません", "みょうではありません", FORMAL_NEGATIVE, lemma);
		testInflection(tokens, pos, translation, "妙だったら", "みょうだったら", PROVISIONAL, lemma);
	}

	@Test
	public void testGoBu() {
		String lemma = GO_BU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(17, tokens.size());
		String pos = "V1[go-bu]";
		String translation = "1) to fly 2) to jump 3) to go quickly 4) to splash, splatter";

		testInflection(tokens, pos, translation, "飛ば", "とば", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "飛べ", "とべ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "飛ばせる", "とばせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "飛ばず", "とばず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "飛んで", "とんで", CONJUNCTIVE, lemma);
	}

	@Test
	public void testGoRu() {
		String lemma = GO_RU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(17, tokens.size());
		String pos = "VI1[go-ru]";
		String translation = "1) to be conveyed, to be communicated";

		testInflection(tokens, pos, translation, "伝わる", "つたわる", TERMINAL, lemma);
	}

	@Test
	public void testHonorific() {
		String lemma = HONORIFIC_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(18, tokens.size());
		String pos = "V1[honorific]";
		String translation = "1) give";

		testInflection(tokens, pos, translation, "下さら", "くださら", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "下さい", "ください", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "下さらせる", "くださらせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "下さらず", "くださらず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "下さって", "くださって", CONJUNCTIVE, lemma);
	}

	@Test
	public void testIchi() {
		String lemma = ICHI_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(21, tokens.size());
		String pos = "VT2[ichi]";
		String translation = "1) come about, occur, arise 2) result from, be caused by";

		testInflection(tokens, pos, translation, "生じ", "しょうじ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "生じよ", "しょうじよ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "生じさせる", "しょうじさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "生じず", "しょうじず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "生じて", "しょうじて", CONJUNCTIVE, lemma);
	}

	@Test
	public void testSuruIKu() {
		String lemma = SURU_I_KU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(23, tokens.size());
		String pos = "V3[suru-i-ku]";
		String translation = "1) face each other 2) be in response to, be against";

		testInflection(tokens, pos, translation, "対さ", "たいさ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "対せ", "たいせ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "対させる", "たいさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "対さず", "たいさず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "対して", "たいして", CONJUNCTIVE, lemma);
	}

	@Test
	public void testSuruTsu() {
		String lemma = SURU_TSU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(20, tokens.size());
		String pos = "V3[suru-tsu]";
		String translation = "1) guess, presume, sense";

		testInflection(tokens, pos, translation, "察せ", "さっせ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "察しろ", "さっしろ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "察しさせる", "さっしさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "察せず", "さっせず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "察して", "さっして", CONJUNCTIVE, lemma);
	}

	@Test
	public void testSuru() {
		String lemma = SURU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(18, tokens.size());
		String pos = "VB3[suru]";
		String translation = "1) meet, assemble, gather 2) mediate";

		testInflection(tokens, pos, translation, "会し", "かいし", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "会せよ", "かいせよ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "会させる", "かいさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "会せず", "かいせず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "会して", "かいして", CONJUNCTIVE, lemma);
	}

	@Test
	public void testZuru() {
		String lemma = ZURU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(16, tokens.size());
		String pos = "VT3[zuru]";
		String translation = "1) believe, put trust in";

		testInflection(tokens, pos, translation, "信じ", "しんじ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "信じろ", "しんじろ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "信じさせる", "しんじさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "信じず", "しんじず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "信じて", "しんじて", CONJUNCTIVE, lemma);
	}

	@Test
	public void testKuru() {
		String lemma = KURU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(17, tokens.size());
		String pos = "VI1[kuru]";
		String translation = "1) bring (someone to a personplace)";

		testInflection(tokens, pos, translation, "連れて来", "つれてこ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "連れて来い", "つれてこい", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "連れて来させる", "つれてこさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "連れて来ず", "つれてこず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "連れて来て", "つれてきて", CONJUNCTIVE, lemma);
	}

	@Test
	public void testAru() {
		String lemma = ARU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(18, tokens.size());
		String pos = "V[aru]";
		String translation = "1) to exist (''inanimate objects'') 2) to be (''inanimate objects'') 3) to have (''inanimate objects'') 4) (of an accident) to happen";

		testInflection(tokens, pos, translation, "あら", "あら", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "あれ", "あれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "あらせる", "あらせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "なくて", "なくて", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "あって", "あって", CONJUNCTIVE, lemma);
	}

	@Test
	public void testBeshi() {
		String lemma = BESHI_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(13, tokens.size());
		String pos = "V[beshi]";
		String translation = "1) must, shall ####*####*##*";

		testInflection(tokens, pos, translation, "べから", "べから", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "べかる", "べかる", ATTRIBUTIVE2, lemma);
		testInflection(tokens, pos, translation, "べからず", "べからず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "べきで", "べきで", CONJUNCTIVE, lemma);

		for (InflectedToken tok : tokens) {
			assertFalse(IMPERATIVE.toString().equals(tok.getInflection()));
			assertFalse(PASSIVE.toString().equals(tok.getInflection()));
			assertFalse(CAUSATIVE.toString().equals(tok.getInflection()));
			assertFalse(POTENTIAL.toString().equals(tok.getInflection()));
			assertFalse(VOLITIONAL.toString().equals(tok.getInflection()));
			assertFalse(FORMAL.toString().equals(tok.getInflection()));
		}
	}

	@Test
	public void testKureru() {
		String lemma = KURERU_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(20, tokens.size());
		String pos = "V[kureru]";
		String translation = "1) to give 2) to do for someone";

		testInflection(tokens, pos, translation, "くれ", "くれ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "くれ", "くれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "くれさせる", "くれさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "くれず", "くれず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "くれて", "くれて", CONJUNCTIVE, lemma);
	}

	@Test
	public void testSuruIndep() {
		String lemma = SURU_INDEP_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(18, tokens.size());
		String pos = "V[suru-indep]";
		String translation = "1) . [rare]";

		testInflection(tokens, pos, translation, "為", "し", IMPERFECTIVE2, lemma);
		testInflection(tokens, pos, translation, "為よ", "せよ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "為せる", "させる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "為ず", "せず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "為て", "して", CONJUNCTIVE, lemma);
	}

	@Test
	public void testTari() {
		String lemma = TARI_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(8, tokens.size());
		String pos = "A[tari]";
		String translation = "1) wretched, miserable 2) terrible, extremely bad 3) going to great efforts";

		testInflection(tokens, pos, translation, "惨憺たる", "さんたんたる", ATTRIBUTIVE, lemma);
	}

	@Test
	public void testQuestionMarkI() {
		String lemma = QUESTION_MARK_I_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "A[i]";
		String translation = "1) beautiful [archaic]";

		testInflection(tokens, pos, translation, "美しかろ", "うつくしかろ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "美しかれ", "うつくしかれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "美しかった", "うつくしかった", INFORMAL_PAST, lemma);
		testInflection(tokens, pos, translation, "美しくないです", "うつくしくないです", FORMAL_NEGATIVE, lemma);
		testInflection(tokens, pos, translation, "美しかったら", "うつくしかったら", PROVISIONAL, lemma);
	}

	@Test
	public void testQuestionMarkNa() {
		String lemma = QUESTION_MARK_NA_TOK;
		List<InflectedToken> tokens = map.get(lemma);
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		String pos = "A[na]";
		String translation = "1) be like, look like, seem like, as if, having the likeness of";

		testInflection(tokens, pos, translation, "様だろ", "ようだろ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "様であれ", "ようであれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "様だった", "ようだった", INFORMAL_PAST, lemma);
		testInflection(tokens, pos, translation, "様ではありません", "ようではありません", FORMAL_NEGATIVE, lemma);
		testInflection(tokens, pos, translation, "様だったら", "ようだったら", PROVISIONAL, lemma);
	}
}

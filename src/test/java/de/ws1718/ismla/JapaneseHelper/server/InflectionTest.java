package de.ws1718.ismla.JapaneseHelper.server;

import static de.ws1718.ismla.JapaneseHelper.server.Inflection.ATTRIBUTIVE;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.ATTRIBUTIVE2;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.CAUSATIVE;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.CONJUNCTIVE;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.FORMAL;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.FORMAL_NEGATIVE;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.IMPERATIVE;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.IMPERFECTIVE;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.IMPERFECTIVE2;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.INFORMAL_PAST;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.NEGATIVE_CONTINUATIVE;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.PASSIVE;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.POTENTIAL;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.PROVISIONAL;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.TERMINAL;
import static de.ws1718.ismla.JapaneseHelper.server.Inflection.VOLITIONAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jetty.websocket.api.SuspendToken;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Multimap;

import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class InflectionTest {

	private static final String RESOURCES_PATH = "src/main/webapp/";
	private static final Logger logger = Logger.getLogger(InflectionTest.class.getSimpleName());

	private static Map<String, List<InflectedToken>> map = new HashMap<String, List<InflectedToken>>();

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

		logger.info("reading tokens");
		List<String> inflectionFiles = getFilesInDir(RESOURCES_PATH + LookupServiceImpl.INFLECTION_TEMPLATES_PATH);
		List<InputStream> inflectionStreams = toStreams(inflectionFiles);
		List<String> dictionaryFiles = getFilesInDir(RESOURCES_PATH + LookupServiceImpl.DICTIONARY_PATH);
		List<InputStream> dictionaryStreams = toStreams(dictionaryFiles);

		WiktionaryPreprocessor wp = new WiktionaryPreprocessor(inflectionFiles, inflectionStreams, dictionaryStreams);
		Multimap<String, Token> tokenMap = wp.getTokens();

		logger.info("updating map");
		for (Token tok : tokenMap.values()) {
			if (tok instanceof InflectedToken) {
				InflectedToken infTok = (InflectedToken) tok;
				String lemma = infTok.getLemmaToken().getForm();
				if (keys.contains(lemma)) {
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

	private static List<String> getFilesInDir(String directory) {
		List<String> files = new ArrayList<>();
		try (Stream<Path> dir = Files.walk(Paths.get(directory))) {
			files = dir.filter(Files::isRegularFile).map(file -> file.toString()).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return files;
	}

	private static List<InputStream> toStreams(List<String> filenames) {
		List<InputStream> streams = new ArrayList<>();
		for (String file : filenames) {
			try {
				streams.add(new FileInputStream(new File(file)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				fail();
			}
		}
		return streams;
	}

	private void testInflection(List<InflectedToken> tokens, String pos, String translation, String form,
			String pronunciation, Inflection inflection, Token lemma) {
		assertTrue(tokens.contains(new InflectedToken(lemma, form, pronunciation, inflection)));
	}

	@Test
	public void testI() {
		String lemmaForm = I_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "A[i]";
		String translation = "1) bright, light, luminous 2) merry, cheerful";
		Token lemma = new Token(lemmaForm, "あかるい", pos, translation);
		testInflection(tokens, pos, translation, "明るかろ", "あかるかろ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "明るかれ", "あかるかれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "明るかった", "あかるかった", INFORMAL_PAST, lemma);
		testInflection(tokens, pos, translation, "明るくないです", "あかるくないです", FORMAL_NEGATIVE, lemma);
		testInflection(tokens, pos, translation, "明るかったら", "あかるかったら", PROVISIONAL, lemma);
	}

	@Test
	public void testNa() {
		String lemmaForm = NA_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		String pos = "A[na]";
		String translation = "1) strange 2) excellent";
		Token lemma = new Token(lemmaForm, "みょう", pos, translation);
		testInflection(tokens, pos, translation, "妙だろ", "みょうだろ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "妙であれ", "みょうであれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "妙だった", "みょうだった", INFORMAL_PAST, lemma);
		testInflection(tokens, pos, translation, "妙ではありません", "みょうではありません", FORMAL_NEGATIVE, lemma);
		testInflection(tokens, pos, translation, "妙だったら", "みょうだったら", PROVISIONAL, lemma);
	}

	@Test
	public void testGoBu() {
		String lemmaForm = GO_BU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(18, tokens.size());
		String pos = "V1[go-bu]";
		String translation = "1) to fly 2) to jump 3) to go quickly 4) to splash, splatter";
		Token lemma = new Token(lemmaForm, "とぶ", pos, translation);
		testInflection(tokens, pos, translation, "飛ば", "とば", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "飛べ", "とべ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "飛ばせる", "とばせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "飛ばず", "とばず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "飛んで", "とんで", CONJUNCTIVE, lemma);
		testInflection(tokens, pos, translation, "飛びません", "とびません", FORMAL_NEGATIVE, lemma);
	}

	@Test
	public void testGoRu() {
		String lemmaForm = GO_RU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(18, tokens.size());
		String pos = "VI1[go-ru]";
		String translation = "1) to be conveyed, to be communicated";
		Token lemma = new Token(lemmaForm, "つたわる", pos, translation);
		testInflection(tokens, pos, translation, "伝わる", "つたわる", TERMINAL, lemma);
	}

	@Test
	public void testHonorific() {
		String lemmaForm = HONORIFIC_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "V1[honorific]";
		String translation = "1) give";
		Token lemma = new Token(lemmaForm, "くださる", pos, translation);
		testInflection(tokens, pos, translation, "下さら", "くださら", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "下さい", "ください", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "下さらせる", "くださらせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "下さらず", "くださらず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "下さって", "くださって", CONJUNCTIVE, lemma);
	}

	@Test
	public void testIchi() {
		String lemmaForm = ICHI_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(22, tokens.size());
		String pos = "VT2[ichi]";
		String translation = "1) come about, occur, arise 2) result from, be caused by";
		Token lemma = new Token(lemmaForm, "しょうじる", pos, translation);
		testInflection(tokens, pos, translation, "生じ", "しょうじ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "生じよ", "しょうじよ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "生じさせる", "しょうじさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "生じず", "しょうじず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "生じて", "しょうじて", CONJUNCTIVE, lemma);
	}

	@Test
	public void testSuruIKu() {
		String lemmaForm = SURU_I_KU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		String pos = "V3[suru-i-ku]";
		String translation = "1) face each other 2) be in response to, be against";
		Token lemma = new Token(lemmaForm, "たいする", pos, translation);
		testInflection(tokens, pos, translation, "対さ", "たいさ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "対せ", "たいせ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "対させる", "たいさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "対さず", "たいさず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "対して", "たいして", CONJUNCTIVE, lemma);
	}

	@Test
	public void testSuruTsu() {
		String lemmaForm = SURU_TSU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(21, tokens.size());
		String pos = "V3[suru-tsu]";
		String translation = "1) guess, presume, sense";
		Token lemma = new Token(lemmaForm, "さっする", pos, translation);
		testInflection(tokens, pos, translation, "察せ", "さっせ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "察しろ", "さっしろ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "察しさせる", "さっしさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "察せず", "さっせず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "察して", "さっして", CONJUNCTIVE, lemma);
	}

	@Test
	public void testSuru() {
		String lemmaForm = SURU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "VB3[suru]";
		String translation = "1) meet, assemble, gather 2) mediate";
		Token lemma = new Token(lemmaForm, "かいする", pos, translation);
		testInflection(tokens, pos, translation, "会し", "かいし", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "会せよ", "かいせよ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "会させる", "かいさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "会せず", "かいせず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "会して", "かいして", CONJUNCTIVE, lemma);
	}

	@Test
	public void testZuru() {
		String lemmaForm = ZURU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(17, tokens.size());
		String pos = "VT3[zuru]";
		String translation = "1) believe, put trust in";
		Token lemma = new Token(lemmaForm, "しんずる", pos, translation);
		testInflection(tokens, pos, translation, "信じ", "しんじ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "信じろ", "しんじろ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "信じさせる", "しんじさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "信じず", "しんじず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "信じて", "しんじて", CONJUNCTIVE, lemma);
	}

	@Test
	public void testKuru() {
		String lemmaForm = KURU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(18, tokens.size());
		String pos = "VI1[kuru]";
		String translation = "1) bring (someone to a personplace)";
		Token lemma = new Token(lemmaForm, "つれてくる", pos, translation);
		testInflection(tokens, pos, translation, "連れて来", "つれてこ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "連れて来い", "つれてこい", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "連れて来させる", "つれてこさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "連れて来ず", "つれてこず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "連れて来て", "つれてきて", CONJUNCTIVE, lemma);
	}

	@Test
	public void testAru() {
		String lemmaForm = ARU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "V[aru]";
		String translation = "1) to exist (''inanimate objects'') 2) to be (''inanimate objects'') 3) to have (''inanimate objects'') 4) (of an accident) to happen";
		Token lemma = new Token(lemmaForm, lemmaForm, pos, translation);
		testInflection(tokens, pos, translation, "あら", "あら", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "あれ", "あれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "あらせる", "あらせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "なくて", "なくて", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "あって", "あって", CONJUNCTIVE, lemma);
		testInflection(tokens, pos, translation, "ありません", "ありません", FORMAL_NEGATIVE, lemma);
	}

	@Test
	public void testBeshi() {
		String lemmaForm = BESHI_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(13, tokens.size());
		String pos = "V[beshi]";
		String translation = "1) must, shall ####*####*##*";
		Token lemma = new Token(lemmaForm, lemmaForm, pos, translation);
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
		String lemmaForm = KURERU_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(21, tokens.size());
		String pos = "V[kureru]";
		String translation = "1) to give 2) to do for someone";
		Token lemma = new Token(lemmaForm, lemmaForm, pos, translation);
		testInflection(tokens, pos, translation, "くれ", "くれ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "くれ", "くれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "くれさせる", "くれさせる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "くれず", "くれず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "くれて", "くれて", CONJUNCTIVE, lemma);
	}

	@Test
	public void testSuruIndep() {
		String lemmaForm = SURU_INDEP_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "V[suru-indep]";
		String translation = "1) . [rare]";
		Token lemma = new Token(lemmaForm, "する", pos, translation);
		testInflection(tokens, pos, translation, "為", "し", IMPERFECTIVE2, lemma);
		testInflection(tokens, pos, translation, "為よ", "せよ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "為せる", "させる", CAUSATIVE, lemma);
		testInflection(tokens, pos, translation, "為ず", "せず", NEGATIVE_CONTINUATIVE, lemma);
		testInflection(tokens, pos, translation, "為て", "して", CONJUNCTIVE, lemma);
		testInflection(tokens, pos, translation, "為ません", "しません", FORMAL_NEGATIVE, lemma);
	}

	@Test
	public void testTari() {
		String lemmaForm = TARI_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(8, tokens.size());
		String pos = "A[tari]";
		String translation = "1) wretched, miserable 2) terrible, extremely bad 3) going to great efforts";
		Token lemma = new Token(lemmaForm, "さんたん", pos, translation);
		testInflection(tokens, pos, translation, "惨憺たる", "さんたんたる", ATTRIBUTIVE, lemma);
	}

	@Test
	public void testQuestionMarkI() {
		String lemmaForm = QUESTION_MARK_I_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "A[i]";
		String translation = "1) beautiful [archaic]";
		Token lemma = new Token(lemmaForm, "うつくしい", pos, translation);
		testInflection(tokens, pos, translation, "美しかろ", "うつくしかろ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "美しかれ", "うつくしかれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "美しかった", "うつくしかった", INFORMAL_PAST, lemma);
		testInflection(tokens, pos, translation, "美しくないです", "うつくしくないです", FORMAL_NEGATIVE, lemma);
		testInflection(tokens, pos, translation, "美しかったら", "うつくしかったら", PROVISIONAL, lemma);
	}

	@Test
	public void testQuestionMarkNa() {
		String lemmaForm = QUESTION_MARK_NA_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		String pos = "A[na]";
		String translation = "1) be like, look like, seem like, as if, having the likeness of";
		Token lemma = new Token(lemmaForm, "よう", pos, translation);
		testInflection(tokens, pos, translation, "様だろ", "ようだろ", IMPERFECTIVE, lemma);
		testInflection(tokens, pos, translation, "様であれ", "ようであれ", IMPERATIVE, lemma);
		testInflection(tokens, pos, translation, "様だった", "ようだった", INFORMAL_PAST, lemma);
		testInflection(tokens, pos, translation, "様ではありません", "ようではありません", FORMAL_NEGATIVE, lemma);
		testInflection(tokens, pos, translation, "様だったら", "ようだったら", PROVISIONAL, lemma);
	}
}

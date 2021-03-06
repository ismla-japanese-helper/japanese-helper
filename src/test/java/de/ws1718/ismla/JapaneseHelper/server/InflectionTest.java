package de.ws1718.ismla.JapaneseHelper.server;

import static de.ws1718.ismla.JapaneseHelper.shared.Inflection.*;
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

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Multimap;

import de.ws1718.ismla.JapaneseHelper.shared.InflectableToken;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Inflection;
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
	private static final String DA_TOK = "だ";
	private static final String TARI_TOK = "惨憺";
	private static final String NO_INFL_SURU_TOK = "貸し借りなしにする";
	private static final String NO_INFL_GO_U_TOK = "贖う";

	@BeforeClass
	public static void readTokens() throws FileNotFoundException {
		List<String> keys = new ArrayList<String>(Arrays.asList(I_TOK, NA_TOK, GO_BU_TOK, GO_RU_TOK, HONORIFIC_TOK,
				ICHI_TOK, SURU_I_KU_TOK, SURU_TSU_TOK, SURU_TOK, ZURU_TOK, KURU_TOK, ARU_TOK, BESHI_TOK, KURERU_TOK,
				SURU_INDEP_TOK, DA_TOK, TARI_TOK, NO_INFL_SURU_TOK, NO_INFL_GO_U_TOK));

		logger.info("reading tokens");
		List<String> inflectionFiles = getFilesInDir(RESOURCES_PATH + Listener.INFLECTION_TEMPLATES_PATH);
		List<InputStream> inflectionStreams = toStreams(inflectionFiles);
		List<String> dictionaryFiles = getFilesInDir(RESOURCES_PATH + Listener.DICTIONARY_PATH);
		List<InputStream> dictionaryStreams = toStreams(dictionaryFiles);

		InputStream difficultyRatingStream = new FileInputStream(RESOURCES_PATH + Listener.DIFFICULTY_RATING_PATH);
		HashMap<String, String> difficultyRatings = Listener.readDifficultyRatings(difficultyRatingStream);

		WiktionaryPreprocessor wp = new WiktionaryPreprocessor(inflectionFiles, inflectionStreams, dictionaryStreams,
				difficultyRatings);
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

	private String generateDifficultyRating(String base, int extraSyllables) {
		// Using a String is not the most efficient approach, but it is ok for
		// this test class.
		for (int i = 0; i < extraSyllables; i++) {
			base += "-*";
		}
		return base;
	}

	private void testInflection(List<InflectedToken> tokens, String pos, String translation, String form,
			String pronunciation, Inflection inflection, InflectableToken lemma, String difficultyRating) {
		InflectedToken tok = new InflectedToken(lemma, form, pronunciation, inflection);
		tok.setDifficultyRating(difficultyRating);
		assertTrue(tokens.contains(tok));
	}

	@Test
	public void testI() {
		String lemmaForm = I_TOK; // 明るい
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "A[i]";
		String translation = "1) bright, light, luminous 2) merry, cheerful";
		InflectableToken lemma = new InflectableToken(lemmaForm, "あかるい", pos, translation);
		String difficulty = "2-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "明るかろ", "あかるかろ", IMPERFECTIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "明るかれ", "あかるかれ", IMPERATIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "明るかった", "あかるかった", INFORMAL_PAST, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "明るくないです", "あかるくないです", FORMAL_NEGATIVE, lemma,
				generateDifficultyRating(difficulty, 4));
		testInflection(tokens, pos, translation, "明るかったら", "あかるかったら", PROVISIONAL, lemma,
				generateDifficultyRating(difficulty, 3));
	}

	@Test
	public void testNa() {
		String lemmaForm = NA_TOK; // 妙
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		String pos = "A[na]";
		String translation = "1) strange 2) excellent";
		InflectableToken lemma = new InflectableToken(lemmaForm, "みょう", pos, translation);
		String difficulty = "S";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "妙だろ", "みょうだろ", IMPERFECTIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "妙であれ", "みょうであれ", IMPERATIVE, lemma,
				generateDifficultyRating(difficulty, 3));
		testInflection(tokens, pos, translation, "妙だった", "みょうだった", INFORMAL_PAST, lemma,
				generateDifficultyRating(difficulty, 3));
		testInflection(tokens, pos, translation, "妙ではありません", "みょうではありません", FORMAL_NEGATIVE, lemma,
				generateDifficultyRating(difficulty, 7));
		testInflection(tokens, pos, translation, "妙だったら", "みょうだったら", PROVISIONAL, lemma,
				generateDifficultyRating(difficulty, 4));
		testInflection(tokens, pos, translation, "妙なら", "みょうなら", CONDITIONAL, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "妙ならば", "みょうならば", CONDITIONAL, lemma,
				generateDifficultyRating(difficulty, 3));
	}

	@Test
	public void testGoBu() {
		String lemmaForm = GO_BU_TOK; // 飛ぶ
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(20, tokens.size());
		String pos = "V1[go-bu]";
		String translation = "1) to fly 2) to jump 3) to go quickly 4) to splash, splatter";
		InflectableToken lemma = new InflectableToken(lemmaForm, "とぶ", pos, translation);
		String difficulty = "4-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "飛ば", "とば", IMPERFECTIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "飛べ", "とべ", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "飛ばせる", "とばせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "飛ばず", "とばず", NEGATIVE_CONTINUATIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "飛んで", "とんで", CONJUNCTIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "飛びません", "とびません", FORMAL_NEGATIVE, lemma,
				generateDifficultyRating(difficulty, 3));
	}

	@Test
	public void testGoRu() {
		String lemmaForm = GO_RU_TOK; // 伝わる
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(20, tokens.size());
		String pos = "VI1[go-ru]";
		String translation = "1) to be conveyed, to be communicated";
		InflectableToken lemma = new InflectableToken(lemmaForm, "つたわる", pos, translation);
		String difficulty = "4-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "伝わる", "つたわる", TERMINAL, lemma, difficulty);
	}

	@Test
	public void testHonorific() {
		String lemmaForm = HONORIFIC_TOK; // 下さる
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(21, tokens.size());
		String pos = "V1[honorific]";
		String translation = "1) give";
		InflectableToken lemma = new InflectableToken(lemmaForm, "くださる", pos, translation);
		String difficulty = "1-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "下さら", "くださら", IMPERFECTIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "下さい", "ください", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "下さらせる", "くださらせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "下さらず", "くださらず", NEGATIVE_CONTINUATIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "下さって", "くださって", CONJUNCTIVE, lemma,
				generateDifficultyRating(difficulty, 1));
	}

	@Test
	public void testIchi() {
		String lemmaForm = ICHI_TOK; // 生じる
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(24, tokens.size());
		String pos = "VT2[ichi]";
		String translation = "1) come about, occur, arise 2) result from, be caused by";
		InflectableToken lemma = new InflectableToken(lemmaForm, "しょうじる", pos, translation);
		String difficulty = "1-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "生じ", "しょうじ", IMPERFECTIVE, lemma, "1-*");
		testInflection(tokens, pos, translation, "生じよ", "しょうじよ", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "生じさせる", "しょうじさせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "生じず", "しょうじず", NEGATIVE_CONTINUATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "生じて", "しょうじて", CONJUNCTIVE, lemma, difficulty);
	}

	@Test
	public void testSuruIKu() {
		String lemmaForm = SURU_I_KU_TOK; // 対する
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(26, tokens.size());
		String pos = "V3[suru-i-ku]";
		String translation = "1) face each other 2) be in response to, be against";
		InflectableToken lemma = new InflectableToken(lemmaForm, "たいする", pos, translation);
		String difficulty = "3-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "対さ", "たいさ", IMPERFECTIVE, lemma, "3-*");
		testInflection(tokens, pos, translation, "対せ", "たいせ", IMPERATIVE, lemma, "3-*");
		testInflection(tokens, pos, translation, "対させる", "たいさせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "対さず", "たいさず", NEGATIVE_CONTINUATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "対して", "たいして", CONJUNCTIVE, lemma, difficulty);
	}

	@Test
	public void testSuruTsu() {
		String lemmaForm = SURU_TSU_TOK; // 察する
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(23, tokens.size());
		String pos = "V3[suru-tsu]";
		String translation = "1) guess, presume, sense";
		InflectableToken lemma = new InflectableToken(lemmaForm, "さっする", pos, translation);
		String difficulty = "4-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "察せ", "さっせ", IMPERFECTIVE, lemma, "4-*");
		testInflection(tokens, pos, translation, "察しろ", "さっしろ", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "察しさせる", "さっしさせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "察せず", "さっせず", NEGATIVE_CONTINUATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "察して", "さっして", CONJUNCTIVE, lemma, difficulty);
	}

	@Test
	public void testSuru() {
		String lemmaForm = SURU_TOK; // 会する
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(21, tokens.size());
		String pos = "VB3[suru]";
		String translation = "1) meet, assemble, gather 2) mediate";
		InflectableToken lemma = new InflectableToken(lemmaForm, "かいする", pos, translation);
		String difficulty = "2-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "会し", "かいし", IMPERFECTIVE, lemma, "2-*");
		testInflection(tokens, pos, translation, "会せよ", "かいせよ", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "会させる", "かいさせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "会せず", "かいせず", NEGATIVE_CONTINUATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "会して", "かいして", CONJUNCTIVE, lemma, difficulty);
	}

	@Test
	public void testZuru() {
		String lemmaForm = ZURU_TOK; // 信ずる
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "VT3[zuru]";
		String translation = "1) believe, put trust in";
		InflectableToken lemma = new InflectableToken(lemmaForm, "しんずる", pos, translation);
		String difficulty = "4-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "信じ", "しんじ", IMPERFECTIVE, lemma, "4-*");
		testInflection(tokens, pos, translation, "信じろ", "しんじろ", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "信じさせる", "しんじさせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "信じず", "しんじず", NEGATIVE_CONTINUATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "信じて", "しんじて", CONJUNCTIVE, lemma, difficulty);
	}

	@Test
	public void testKuru() {
		String lemmaForm = KURU_TOK;// 連れて来る
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(20, tokens.size());
		String pos = "VI1[kuru]";
		String translation = "1) bring (someone to a personplace)";
		InflectableToken lemma = new InflectableToken(lemmaForm, "つれてくる", pos, translation);
		String difficulty = "4-*-*-2-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "連れて来", "つれてこ", IMPERFECTIVE, lemma, "4-*-*-2");
		testInflection(tokens, pos, translation, "連れて来い", "つれてこい", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "連れて来させる", "つれてこさせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "連れて来ず", "つれてこず", NEGATIVE_CONTINUATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "連れて来て", "つれてきて", CONJUNCTIVE, lemma, difficulty);
	}

	@Test
	public void testAru() {
		String lemmaForm = ARU_TOK; // ある
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(21, tokens.size());
		String pos = "VI1[aru]";
		String translation = "1) to exist (''inanimate objects'') 2) to be (''inanimate objects'') 3) to have (''inanimate objects'') 4) (of an accident) to happen";
		InflectableToken lemma = new InflectableToken(lemmaForm, lemmaForm, pos, translation);
		String difficulty = "*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "あら", "あら", IMPERFECTIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "あれ", "あれ", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "あらせる", "あらせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "なくて", "なくて", NEGATIVE_CONTINUATIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "あって", "あって", CONJUNCTIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "ありません", "ありません", FORMAL_NEGATIVE, lemma,
				generateDifficultyRating(difficulty, 3));
	}

	@Test
	public void testBeshi() {
		String lemmaForm = BESHI_TOK; // べし
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(13, tokens.size());
		String pos = "V[beshi]";
		String translation = "1) must, shall ###";
		InflectableToken lemma = new InflectableToken(lemmaForm, lemmaForm, pos, translation);
		String difficulty = "*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "べから", "べから", IMPERFECTIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "べかる", "べかる", ATTRIBUTIVE2, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "べからず", "べからず", NEGATIVE_CONTINUATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "べきで", "べきで", CONJUNCTIVE, lemma,
				generateDifficultyRating(difficulty, 1));

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
		String lemmaForm = KURERU_TOK; // くれる
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(23, tokens.size());
		String pos = "V2[kureru]";
		String translation = "1) to give 2) to do for someone";
		InflectableToken lemma = new InflectableToken(lemmaForm, lemmaForm, pos, translation);
		String difficulty = "*-*-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "くれ", "くれ", IMPERFECTIVE, lemma, "*-*");
		testInflection(tokens, pos, translation, "くれ", "くれ", IMPERATIVE, lemma, "*-*");
		testInflection(tokens, pos, translation, "くれさせる", "くれさせる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "くれず", "くれず", NEGATIVE_CONTINUATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "くれて", "くれて", CONJUNCTIVE, lemma, difficulty);
	}

	@Test
	public void testSuruIndep() {
		String lemmaForm = SURU_INDEP_TOK; // 為る
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		tokens = tokens.stream().filter(tok -> tok.getInflectionParadigm().equals("suru-indep"))
				.collect(Collectors.toList());
		// 21, not 23, because the kanji version skips two imperative forms
		assertEquals(21, tokens.size());
		String pos = "VT3[suru-indep]";
		String translation = "1) . [rare]";
		InflectableToken lemma = new InflectableToken(lemmaForm, "する", pos, translation);
		String difficulty = "S-*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "為", "し", IMPERFECTIVE2, lemma, "S");
		testInflection(tokens, pos, translation, "為よ", "せよ", IMPERATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "為せる", "させる", CAUSATIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "為ず", "せず", NEGATIVE_CONTINUATIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "為て", "して", CONJUNCTIVE, lemma, difficulty);
		testInflection(tokens, pos, translation, "為ません", "しません", FORMAL_NEGATIVE, lemma,
				generateDifficultyRating(difficulty, 2));
	}

	@Test
	public void testDa() {
		String lemmaForm = DA_TOK; // だ
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(19, tokens.size());
		String pos = "V[da]";
		String translation = "1) [A particle used when a sentence has a nominal as its predicate, "
				+ "usually but not always equal to the English verb \"to be\".]";
		InflectableToken lemma = new InflectableToken(lemmaForm, lemmaForm, pos, translation);
		String difficulty = "*";
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "では", "では", IMPERFECTIVE, lemma,
				generateDifficultyRating(difficulty, 1));
		// Make sure the list only contains the formal inflection that are in
		// the template. Additional formal [negative and/or perfective]
		// inflections should not have been added.
		testInflection(tokens, pos, translation, "です", "です", FORMAL, lemma, generateDifficultyRating(difficulty, 1));
		testInflection(tokens, pos, translation, "でした", "でした", FORMAL_PERFECTIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		testInflection(tokens, pos, translation, "でしょう", "でしょう", FORMAL_VOLITIONAL, lemma,
				generateDifficultyRating(difficulty, 3));
		testInflection(tokens, pos, translation, "でして", "でして", FORMAL_CONJUNCTIVE, lemma,
				generateDifficultyRating(difficulty, 2));
		long numTokensFormal = tokens.stream().filter(tok -> tok.getInflectionInformation().contains("formal")).count();
		assertEquals(4, numTokensFormal);
	}

	@Test
	public void testTari() { // 惨憺
		String lemmaForm = TARI_TOK;
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(2, tokens.size());
		String pos = "A[tari]";
		String translation = "1) wretched, miserable 2) terrible, extremely bad 3) going to great efforts";
		InflectableToken lemma = new InflectableToken(lemmaForm, "さんたん", pos, translation);
		String difficulty = "S-*"; // the second kanji is not in the jouyou list
		lemma.setDifficultyRating(difficulty);
		testInflection(tokens, pos, translation, "惨憺たる", "さんたんたる", ATTRIBUTIVE, lemma,
				generateDifficultyRating(difficulty, 2));
	}

	@Test
	public void testNoInflSuru() {
		String lemmaForm = NO_INFL_SURU_TOK; // 貸し借りなしにする
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		assertEquals(21, tokens.size());
		assertEquals("probably suru", tokens.get(0).getInflectionParadigm());
	}

	@Test
	public void testNoInflGoU() {
		String lemmaForm = NO_INFL_GO_U_TOK; // 贖う
		List<InflectedToken> tokens = map.get(lemmaForm);
		assertNotNull(tokens);
		// 贖う has two entries in the Wiktionary dump.
		// They have different pronunciations but the same POS tag.
		assertEquals(40, tokens.size());
		assertEquals("probably go-u", tokens.get(0).getInflectionParadigm());
	}
}

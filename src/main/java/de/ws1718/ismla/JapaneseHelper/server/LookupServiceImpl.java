package de.ws1718.ismla.JapaneseHelper.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import com.atilika.kuromoji.ipadic.Tokenizer;
import com.google.common.base.Joiner;
import com.google.common.collect.ListMultimap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.mariten.kanatools.KanaConverter;

import de.ws1718.ismla.JapaneseHelper.client.LookupService;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;;

public class LookupServiceImpl extends RemoteServiceServlet implements LookupService {
	private static final long serialVersionUID = 568570423376066244L;

	private static final Logger logger = Logger.getLogger(LookupServiceImpl.class.getSimpleName());

	@SuppressWarnings("unchecked")
	public List<ArrayList<Token>> lookup(String sentence) {
		ListMultimap<String, Token> tokenMap = (ListMultimap<String, Token>) getServletContext()
				.getAttribute("tokenMap");
		Tokenizer tokenizer = new Tokenizer();
		// This is the Token defined by the Kuromoji parser.
		List<com.atilika.kuromoji.ipadic.Token> ipaTokens = tokenizer.tokenize(sentence.trim());

		// This is the Token defined by us.
		List<ArrayList<Token>> results = convertTokens(ipaTokens, tokenMap);

		return results;
	}

	@SuppressWarnings("unchecked")
	public String tokenizeFiles() {
		List<FileInputStream> streams = (List<FileInputStream>) getServletContext().getAttribute("tokenizationStreams");
		List<String> files = (List<String>) getServletContext().getAttribute("tokenizationFiles");
		System.out.println(streams.size() + ", " + files.size());

		String line;
		for (int i = 0; i < streams.size(); i++) {
			FileInputStream fis = streams.get(i);
			String filename = files.get(i);
			String[] sections = filename.split("/");
			filename = sections[sections.length - 1];
			logger.info("Reading " + filename);
			filename = filename.split("\\.")[0] + "-tokenized.txt";
			try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
					PrintWriter pw = new PrintWriter(new File(filename))) {
				while ((line = br.readLine()) != null) {
					line = line.trim();
					List<ArrayList<Token>> results = lookup(line);
					// System.out.println(line + " " + results.size());
					StringBuilder sb = new StringBuilder();
					String joiner = "";
					for (ArrayList<Token> tokens : results) {
						Token tok = tokens.get(0);
						sb.append(joiner);
						joiner = " ";
						sb.append(tok.getForm());
					}
					pw.println(sb.toString());
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	private List<ArrayList<Token>> convertTokens(List<com.atilika.kuromoji.ipadic.Token> ipaTokens,
			ListMultimap<String, Token> tokenMap) {
		List<ArrayList<Token>> tokens = new ArrayList<>();

		for (int index = 0; index < ipaTokens.size(); index++) {
			com.atilika.kuromoji.ipadic.Token tok = ipaTokens.get(index);
			String form = tok.getSurface();
			String pos = tok.getPartOfSpeechLevel1();
			String pron = tok.getReading();
			// logger.info(form + "\t" + tok.getAllFeatures());

			List<Token> dictTokens = tokenMap.get(tok.getSurface());

			// If the token is inflected, try to lookup the full inflection form
			// instead of displaying several segmented tokens.
			if (!tok.getConjugationForm().equals("*")) {
				// logger.info("Attempting to get inflection suffixes for " +
				// tok.getSurface());
				Joiner joiner = Joiner.on("");
				List<String> multiTokenForm = new ArrayList<>();
				multiTokenForm.add(tok.getSurface());
				List<String> multiTokenPron = new ArrayList<>();
				multiTokenPron.add(tok.getReading());
				// Should look at the token immediately following it.
				int curIndex = index + 1;
				// If it's not out of bounds and it's also marked as an
				// inflection form.
				while (curIndex < ipaTokens.size() && !ipaTokens.get(curIndex).getConjugationForm().equals("*")) {
					multiTokenForm.add(ipaTokens.get(curIndex).getSurface());
					multiTokenPron.add(ipaTokens.get(curIndex).getReading());
					curIndex++;
				}

				while (multiTokenForm.size() > 1) {
					List<Token> multiTokenEntry = tokenMap.get(joiner.join(multiTokenForm));
					if (multiTokenEntry != null && multiTokenEntry.size() > 0) {
						dictTokens = multiTokenEntry;
						// Skip all the consumed tokens from the Kuromoji
						// outputs of course.
						// -1 because the outer loop will still + 1
						index = index + multiTokenForm.size() - 1;
						break;
					}

					// Else we try again with one less entry in the multiToken,
					// i.e. we might have overreached in the search.
					multiTokenForm.remove(multiTokenForm.size() - 1);
					multiTokenPron.remove(multiTokenPron.size() - 1);
				}

				form = joiner.join(multiTokenForm);
				pron = joiner.join(multiTokenPron);
				// logger.info("Continuing with " + form);
			}

			// Sort the results if there are several matches.
			ArrayList<Token> sortedTokens = sortTokens(form, pos, pron, dictTokens);
			tokens.add(sortedTokens);
		}

		return tokens;
	}

	/**
	 * Sorts a list of Token instances by how closely they match the Kuromoji
	 * token (descending order).
	 * 
	 * @param form
	 *            the surface form of the token
	 * @param posKuromoji
	 *            the IPAdic POS tag belonging to the token
	 * @param pronKuromoji
	 *            the reading associated with the token
	 * @param dictTokens
	 *            the list of Wiktionary tokens (can be empty or null)
	 * @return the sorted list
	 */
	// public for testing
	public static ArrayList<Token> sortTokens(String form, String posKuromoji, String pronKuromoji,
			List<Token> dictTokens) {
		// We need to create new variables, otherwise the compiler won't pick up
		// on the fact that the conversion methods are static too, and the
		// Comparator does not compile.
		String posK = convertIPADicPOSTag(posKuromoji);
		String pronK = convertPronunciation(pronKuromoji);

		// logger.info(posK + "\t" + pronK);
		ArrayList<Token> sortedTokens = new ArrayList<>();

		if (dictTokens == null || dictTokens.isEmpty()) {
			String meaning = "1) [out-of-vocabulary]";
			String difficultyRating = "N/A";
			if (posK.equals("PNC")) {
				meaning = "1) [punctuation mark]";
				difficultyRating = "*";
			}
			Token tok = new Token(form, pronK, posK, meaning);
			tok.setDifficultyRating(difficultyRating);
			// logger.info("no matches, created token: " + tok);
			return new ArrayList<Token>(Arrays.asList(tok));
		}

		// primary sort order:
		// Try to match the POS tag with that of the Kuromoji token.
		Comparator<Token> comp = Comparator.comparing(Token::getPos, (pos1, pos2) -> {
			pos1 = convertWiktionaryPOSTag(pos1);
			pos2 = convertWiktionaryPOSTag(pos2);
			return pos1.equals(pos2) ? 0 : pos1.equals(posK) ? -1 : 1;
		}).thenComparing(Token::getPronunciation, (pron1, pron2) -> {
			// secondary sort order:
			// Try to match the pronunciation with that of the Kuromoji token.
			pron1 = convertPronunciation(pron1);
			pron2 = convertPronunciation(pron2);
			return pron1.equals(pron2) ? 0 : pron1.equals(pronK) ? -1 : 1;
		}).thenComparing(Token::getClass, (class1, class2) -> {
			// tertiary sort order:
			// Prefer inflected tokens over uninflected ones.
			return class1.equals(class2) ? 0 : class1.equals(InflectedToken.class) ? -1 : 1;
		});

		Collections.sort(dictTokens, comp);
		for (Token tok : dictTokens) {
			// logger.info("\t" + tok);
			sortedTokens.add(tok);
		}
		return sortedTokens;
	}

	private static String convertIPADicPOSTag(String ipadicTag) {
		switch (ipadicTag) {
		case "名詞":
			// Several possibilities under this category.
			return "N";
		case "動詞":
			// Apparently Wiktionary differentiates between transitive and
			// intransitive verbs but it's not sure whether IPAdic offers such a
			// distinction.
			return "V";
		case "形容詞":
			return "A";
		case "副詞":
			return "ADV";
		case "接続詞":
			return "CNJ";
		case "感動詞":
			return "ITJ";
		case "助詞":
			return "PRT";
		case "助動詞":
			return "SFX";
		case "連体詞":
			return "DET";
		case "接頭詞":
			return "PFX";
		case "記号":
			return "PNC";
		default:
			// The remaining top-class tags would be
			// "フィラー" ("filler") and "その他" ("etc."),
			// which shouldn't be relevant.
			return "";
		}
	}

	private static String convertWiktionaryPOSTag(String pos) {
		if (pos.startsWith("V")) {
			return "V";
		}
		return pos;
	}

	private static String convertPronunciation(String pron) {
		// Sometimes, the Wiktionary pronunciations include a period to denote
		// kanji boundaries that occur in the middle of long vowels.
		pron = pron.replaceAll("\\.", "");
		// The Kuromoji pronunciations are in katakana; some of the Wiktionary
		// ones are as well. If the pronunciation is already in hiragana, this
		// does not change anything.
		return KanaConverter.convertKana(pron, KanaConverter.OP_ZEN_KATA_TO_ZEN_HIRA);
	}

}
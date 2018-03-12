package de.ws1718.ismla.JapaneseHelper.server;

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
import de.ws1718.ismla.JapaneseHelper.shared.Token;

// First try to look up the base form of the word in the hashmap
// If multiple entries, try POS tags
// Also should try to combine the word with the word after it to see if it forms some inflection form.
public class LookupServiceImpl extends RemoteServiceServlet implements LookupService {
	private static final long serialVersionUID = 568570423376066244L;

	private static final Logger logger = Logger.getLogger(LookupServiceImpl.class.getSimpleName());

	// requires the file inside the directory to have been generated by
	// WiktionaryPreprocessor
	public static final String DICTIONARY_PATH = "/WEB-INF/dictionary/";
	public static final String INFLECTION_TEMPLATES_PATH = "/WEB-INF/inflection-templates/";

	@SuppressWarnings("unchecked")
	public List<Token> lookup(String sentence) {
		ListMultimap<String, Token> tokenMap = (ListMultimap<String, Token>) getServletContext()
				.getAttribute("tokenMap");
		Tokenizer tokenizer = new Tokenizer();
		// This is the Token defined by the Kuromoji parser.
		List<com.atilika.kuromoji.ipadic.Token> ipaTokens = tokenizer.tokenize(sentence.trim());

		// This is the Token defined by us.
		List<Token> results = convertTokens(ipaTokens, tokenMap);

		return results;
	}

	private List<Token> convertTokens(List<com.atilika.kuromoji.ipadic.Token> ipaTokens,
			ListMultimap<String, Token> tokenMap) {
		List<Token> tokens = new ArrayList<>();

		for (int index = 0; index < ipaTokens.size(); index++) {
			com.atilika.kuromoji.ipadic.Token tok = ipaTokens.get(index);
			logger.info(tok.getSurface() + "\t" + tok.getAllFeatures());

			List<Token> dictTokens = tokenMap.get(tok.getSurface());

			// If the token is inflected, try to lookup the full inflection form
			// instead of displaying several segmented tokens.
			if (!tok.getConjugationForm().equals("*")) {
				Joiner joiner = Joiner.on("");
				ArrayList<String> multiToken = new ArrayList<>();
				multiToken.add(tok.getSurface());
				// Should look at the token immediately following it.
				int curIndex = index + 1;
				// If it's not out of bounds and it's also marked as an
				// inflection form.
				while (curIndex < ipaTokens.size() && !ipaTokens.get(curIndex).getConjugationForm().equals("*")) {
					multiToken.add(ipaTokens.get(curIndex).getSurface());
					curIndex++;
				}

				while (multiToken.size() > 1) {
					List<Token> multiTokenEntry = tokenMap.get(joiner.join(multiToken));
					if (multiTokenEntry != null && multiTokenEntry.size() > 0) {
						dictTokens = multiTokenEntry;
						// Skip all the consumed tokens from the Kuromoji
						// outputs of course.
						// -1 because the outer loop will still + 1
						index = index + multiToken.size() - 1;
						break;
					}

					// Else we try again with one less entry in the multiToken,
					// i.e. we might have overreached in the search.
					multiToken.remove(multiToken.size() - 1);
				}
			}

			// Sort the results if there are several matches.
			// Even if we got a multitok, we can still a kind of sort the
			// entries by using the first token in the multitok, which is likely
			// the base form.
			dictTokens = sortTokens(tok, dictTokens);

			// TODO make it possible to show the alternatives (in order) as a
			// pop-up? (issue #20)
			tokens.add(dictTokens.get(0));
		}

		return tokens;
	}

	/**
	 * Sorts a list of Token instances by how closely they match the Kuromoji
	 * token (descending order).
	 * 
	 * @param tokKuromoji
	 *            the Kuromoji token
	 * @param dictTokens
	 *            the list of tokens (can be empty or null)
	 * @return the sorted list
	 */
	// public for testing
	public static List<Token> sortTokens(com.atilika.kuromoji.ipadic.Token tokKuromoji, List<Token> dictTokens) {
		String posKuromoji = convertPOSTag(tokKuromoji);
		String pronKuromoji = convertPronunciation(tokKuromoji.getReading());
		logger.info(posKuromoji + "\t" + pronKuromoji);

		if (dictTokens == null || dictTokens.isEmpty()) {
			String meaning = "1) [out-of-vocabulary]";
			if (posKuromoji.equals("PNC")) {
				meaning = "1) [punctuation mark]";
			}
			return Arrays.asList(new Token(tokKuromoji.getSurface(), pronKuromoji, posKuromoji, meaning));
		}

		// primary sort order: POS tag
		Comparator<Token> comp = Comparator.comparing(Token::getPos, (pos1, pos2) -> {
			pos1 = convertPOSTag(pos1);
			pos2 = convertPOSTag(pos2);
			return pos1.equals(pos2) ? 0 : pos1.equals(posKuromoji) ? -1 : 1;
		}).thenComparing(Token::getPronunciation, (pron1, pron2) -> {
			// secondary sort order: pronunciation
			pron1 = convertPronunciation(pron1);
			pron2 = convertPronunciation(pron2);
			return pron1.equals(pron2) ? 0 : pron1.equals(pronKuromoji) ? -1 : 1;
		});

		Collections.sort(dictTokens, comp);
		for (Token tok : dictTokens) {
			logger.info("\t" + tok);
		}
		return dictTokens;
	}

	private static String convertPOSTag(com.atilika.kuromoji.ipadic.Token t) {
		String ipadicTag = t.getPartOfSpeechLevel1();
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
			// TODO This seems weird. Might need to confirm further.
			return "SFX";
		case "連体詞":
			// TODO Really? This also seems quite weird.
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

	private static String convertPOSTag(String pos) {
		if (pos.startsWith("V")) {
			return "V";
		}
		// TODO: it might be necessary to expand this method (issue #3)
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

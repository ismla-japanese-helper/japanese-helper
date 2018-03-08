package de.ws1718.ismla.JapaneseHelper.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

/**
 * Preprocessing: generate and add the inflections for verbs and adjectives.
 * Needs only to be performed whenever the dictionary dump is updated.
 * 
 * This preprocessing class is separate from the GWT structure and cannot be run
 * be the user of the final application. It is separate because it needs to have
 * write-access to the WEB-INF folder, and because the preprocessing should be
 * done before the user uses the application.
 */
public class WiktionaryPreprocessor {

	private static final Logger logger = Logger.getLogger(WiktionaryPreprocessor.class.getSimpleName());

	private Map<String, Map<Inflection, String>> inflections;
	private Multimap<String, Token> tokens;

	public WiktionaryPreprocessor(List<String> inflectionFilenames, List<InputStream> inflectionStreams,
			List<InputStream> dictionaryStreams) {
		inflections = new HashMap<>();
		tokens = ArrayListMultimap.create();

		setUpInflectionTemplates(inflectionFilenames, inflectionStreams);
		for (InputStream dictionaryStream : dictionaryStreams) {
			readDictionary(dictionaryStream);
		}
		logger.info("read (and generated) " + tokens.size() + " tokens");
	}

	public Multimap<String, Token> getTokens() {
		return tokens;
	}

	private void readDictionary(InputStream is) {
		String line;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			lines: while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("﻿##") || line.startsWith("##")) {
					// the first version contains control characters
					continue;
				}
				String[] fields = line.split("\t");
				if (fields.length < 4) {
					continue;
				}
				String form = fields[0];
				String pronunciation = fields[1];
				String pos = fields[2];
				String translation = fields[3];
				Token tok = new Token(form, pronunciation, pos, translation);
				addToken(tok);

				// Add an additional entry with the dash removed,
				// if the POS is "SFX".
				if (tok.getPos().equals("SFX")) {
					processSFXToken(tok);
				}

				if ("です".equals(form) && "V".equals(pos)) {
					// TODO check if です still doesn't have any inflection tag
					// after the update of the Wiktionary dump
					// if it does have one, move this new token assignment into
					// the switch below
					tok = new Token(form, pronunciation, "V[desu]", translation);
				}

				if (tok.inflects()) {
					switch (tok.getInflectionParadigm()) {
					case "?":
						/*
						 * Some of the adjectives have the inflection tag "?".
						 * Our heuristic of deciding its inflection scheme based
						 * on the final syllable should work correctly in most
						 * cases.
						 */
						tok = new Token(form, pronunciation, (form.endsWith("い") ? "A[i]" : "A[na]"), translation);
						break;
					case "verbconj":
						switch (tok.getForm()) {
						case "有る":
						case "ある":
							tok = new Token(form, pronunciation, "V[aru]", translation);
							break;
						case "する":
						case "為る":
							tok = new Token(form, pronunciation, "V[suru-indep]", translation);
							break;
						case "くれる":
						case "呉れる":
							tok = new Token(form, pronunciation, "V[kureru]", translation);
							break;
						case "べし":
							// The pronunciation is listed as "suffix",
							// so we re-use the kana from the form instead.
							tok = new Token(form, form, "V[beshi]", translation);
							break;
						case "だ":
							// TODO issue #14
							tok = new Token(form, form, "V[da]", translation);
						default:
							continue lines;
						}
						break;
					case "na":
						// The Wiktionary entries end with "(な)".
						form = form.substring(0, form.length() - 3);
						pronunciation = pronunciation.substring(0, pronunciation.length() - 3);
						tok = new Token(form, pronunciation, pos, translation);
						addToken(tok);
					}
					inflect(tok);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addToken(Token tok) {
		tokens.put(tok.getForm(), tok);
	}

	private void inflect(Token tok) {
		String inflectionName = tok.getInflectionParadigm();
		Map<Inflection, String> paradigm = inflections.get(inflectionName);

		for (Entry<Inflection, String> entry : paradigm.entrySet()) {
			Inflection infl = entry.getKey();
			String suffix = entry.getValue();

			// some entries contain optional kana (e.g. "なら（ば）" in ja-na.txt)
			// we create tokens for both versions (e.g. "ならば" and "なら")
			int parOpen = suffix.indexOf("（");
			int parClose = suffix.indexOf("）");
			if (parOpen != -1 && parClose != -1 && parClose > parOpen) {
				// without optional kana
				inflect(tok, suffix.substring(0, parOpen) + suffix.substring(parClose + 1), infl, inflectionName);
				// with optional kana
				inflect(tok, suffix.substring(0, parOpen) + suffix.substring(parOpen + 1, parClose)
						+ suffix.substring(parClose + 1), infl, inflectionName);
			} else {
				// regular entries
				inflect(tok, suffix, infl, inflectionName);
			}
		}
	}

	private void inflect(Token tok, String suffix, Inflection inflection, String inflectionName) {
		String form = tok.getForm();
		String pron = tok.getPronunciation();
		String formInfl = "";
		String pronInfl = "";

		// get the stem of the predicate and add the inflectional suffix
		switch (inflectionName) {
		case "aru":
			// the template ja-aru includes the stem because it is irregular
			if ("有る".equals(form)) {
				formInfl = suffix;
			} else {
				formInfl = aruKanjiToKana(suffix);
			}
			pronInfl = aruKanjiToKana(suffix);
			break;
		case "suru-indep":
			// the template ja-suru-indep includes the stem
			// because it is irregular
			formInfl = suffix;
			pronInfl = suffix;
			if ("為る".equals(form)) {
				// there seems to be only one reading for the
				// imperfective inflection of the kanji version
				if (Inflection.IMPERFECTIVE.equals(inflection)
						|| Inflection.IMPERFECTIVE3.equals(inflection)) {
					return;
				}
				// turn the pure-kana forms into forms containing kanji
				if (suffix.startsWith("で")) {
					formInfl = "出" + suffix.substring(1);
				} else {
					formInfl = "為" + suffix.substring(1);
				}
			}
			break;
		case "kuru":
			/*
			 * A special property of the verb with this inflection scheme is
			 * that the vowel quality of final syllable of its stem (来) changes
			 * for some of the inflections--this kanji is represented by
			 * different kana when transcribing its pronunciation. Therefore, we
			 * included the kana versions of this syllable in the template and
			 * need to avoid including this syllable twice now.
			 */
			pronInfl = pron.substring(0, pron.length() - 2) + suffix;
			if ("連れて来る".equals(form)) {
				formInfl = form.substring(0, form.length() - 1) + suffix.substring(1);
			} else {
				formInfl = pronInfl;
			}
			break;
		case "suru-i-ku":
		case "suru-tsu":
		case "suru":
		case "zuru":
			// remove the final する to get the stem
			formInfl = form.substring(0, form.length() - 2) + suffix;
			pronInfl = pron.substring(0, pron.length() - 2) + suffix;
			break;
		case "na":
		case "tari":
			formInfl = form + suffix;
			pronInfl = pron + suffix;
			break;
		default:
			// remove the final syllable to get the stem
			formInfl = form.substring(0, form.length() - 1) + suffix;
			pronInfl = pron.substring(0, pron.length() - 1) + suffix;
		}

		formInfl = removeWhiteSpace(formInfl);
		pronInfl = removeWhiteSpace(pronInfl);
		addToken(new InflectedToken(tok, formInfl, pronInfl, inflection));
	}

	private void processSFXToken(Token t) {
		addToken(new Token(t.getForm().replaceAll("-", ""), t.getPronunciation().replaceAll("-", ""), t.getPos(),
				t.getInflectionParadigm(), t.getTranslations()));
	}

	private String removeWhiteSpace(String word) {
		return word.replaceAll("\\s+", "");
	}

	private String aruKanjiToKana(String word) {
		return word.replace('有', 'あ').replace('無', 'な');
	}

	private void setUpInflectionTemplates(List<String> inflectionFilenames, List<InputStream> inflectionStreams) {
		logger.info("Found the following " + inflectionFilenames.size() + " inflection templates:");
		logger.info(inflectionFilenames.stream().map(file -> new File(file).getName()).collect(Collectors.toList())
				.toString());

		for (int i = 0; i < inflectionFilenames.size(); i++) {
			setUpTemplate(inflectionFilenames.get(i), inflectionStreams.get(i));
		}

		logger.fine("Read the following " + inflections.size() + " inflection maps:");
		for (Entry<String, Map<Inflection, String>> entry : inflections.entrySet()) {
			logger.fine(entry.getKey() + "-->" + entry.getValue());
		}
	}

	private void setUpTemplate(String filename, InputStream is) {
		Map<Inflection, String> inflectionScheme = new HashMap<>();
		String line;

		try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("﻿##") || line.startsWith("##") || !line.contains("=")) {
					// the first version contains control characters
					continue;
				}
				line = line.replaceAll("[->\\|<!{}]", "");
				String[] fields = line.split("=");
				String key = fields[0];
				String suffix;
				if (fields.length == 1) {
					suffix = "";
				} else {
					suffix = fields[1];
					suffix = suffix.replaceAll("\\w", "");
				}
				try {
					Inflection vf = Inflection.valueOf(key.trim().toUpperCase());
					String ending = suffix.trim().replaceAll("\\. ", "");
					inflectionScheme.put(vf, ending);

				} catch (IllegalArgumentException e) {
					if (!line.contains("include") && !line.contains("lemma") && !line.contains("kana")
							&& (!line.contains("note"))) {
						logger.warning("Could not parse the following line: " + line);
					}
					continue;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		inflections.put(getFileName(filename), inflectionScheme);
	}

	private String getFileName(String filename) {
		return new File(filename).getName().replaceAll("(ja-)|(\\.txt)", "");
	}

}

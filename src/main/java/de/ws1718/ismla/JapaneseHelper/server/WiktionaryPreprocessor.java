package de.ws1718.ismla.JapaneseHelper.server;

import static de.ws1718.ismla.JapaneseHelper.shared.Inflection.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import de.ws1718.ismla.JapaneseHelper.shared.InflectableToken;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Inflection;
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

	private Map<String, List<Entry<Inflection, String>>> inflections;
	private ListMultimap<String, Token> tokens;

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

	public ListMultimap<String, Token> getTokens() {
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
				String posAndInflection = fields[2];
				String translation = fields[3];
				Token tok = new Token(form, pronunciation, posAndInflection, translation);

				// If the token is a verb or adjective,
				// we might need to do some additional preprocessing:
				if ("です".equals(form) && "V".equals(posAndInflection)) {
					tok.setInflectionParadigm("desu");
				}
				if (tok.inflects()) {
					switch (tok.getInflectionParadigm()) {
					case "ichidan":
						tok.setInflectionParadigm("ichi");
						break;
					case "ichi":
						if (form.equals("居る")) {
							// Add the (more common) kana version いる.
							inflect(new InflectableToken(pronunciation, pronunciation, posAndInflection, translation));
						}
						break;
					case "verbconj":
					case "verbconj-auto":
						switch (form) {
						case "有る":
						case "ある":
							tok.setInflectionParadigm("aru");
							break;
						case "くれる":
						case "べし":
							tok.setInflectionParadigm("beshi");
							// The pronunciation is listed as "suffix",
							// so we re-use the kana from the form instead.
							tok.setPronunciation(form);
							break;
						case "だ":
							tok.setInflectionParadigm("da");
							break;
						case "出来る":
							tok.setInflectionParadigm("dekiru");
							break;
						case "行く":
							tok.setInflectionParadigm("iku");
							break;
						case "呉れる":
							tok.setInflectionParadigm("kureru");
							break;
						case "や":
							tok.setInflectionParadigm("ya");
							break;
						case "する":
						case "為る":
							tok.setInflectionParadigm("suru-indep");
							break;
						default:
							logger.warning("Could not assign a proper inflection paradigm to " + tok);
							continue lines;
						}
						break;
					case "na":
						// Some of the Wiktionary entries end in "(な)".
						tok.setForm(form.replaceAll("\\(な\\)", ""));
						tok.setPronunciation(pronunciation.replaceAll("\\(な\\)", ""));
					}

					// Generate and add the inflections.
					InflectableToken tokInfl = new InflectableToken(tok);
					inflect(tokInfl);
					addToken(tokInfl);
				} else {

					// And finally, add the actual token.
					addToken(tok);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void addToken(Token tok) {
		tok.setForm(cleanString(tok.getForm()));
		tok.setPronunciation(cleanString(tok.getPronunciation()));
		tokens.put(tok.getForm(), tok);
	}

	private String cleanString(String word) {
		return word.replaceAll("[-\\.\\s+]", "");
	}

	private void inflect(InflectableToken tok) {
		String inflectionName = tok.getInflectionParadigm();
		List<Entry<Inflection, String>> paradigm = inflections.get(inflectionName);

		if (paradigm == null) {
			logger.warning("Could not find an inflection paradigm for \"" + inflectionName + "\".");
			return;
		}

		for (Entry<Inflection, String> entry : paradigm) {
			Inflection infl = entry.getKey();
			String suffix = entry.getValue();

			// Some entries contain optional kana (e.g. "なら（ば）" in ja-na.txt).
			// We create tokens for both versions (e.g. "ならば" and "なら"):
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

	private void inflect(InflectableToken tok, String suffix, Inflection inflection, String inflectionName) {
		String form = tok.getForm();
		String pron = tok.getPronunciation();
		String formInfl = "";
		String pronInfl = "";

		// Get the stem of the predicate and add the inflectional suffix.
		switch (inflectionName) {
		case "aru":
			// The template ja-aru includes the stem because it is irregular.
			if ("有る".equals(form)) {
				formInfl = suffix;
			} else {
				formInfl = aruKanjiToKana(suffix);
			}
			pronInfl = aruKanjiToKana(suffix);
			break;
		case "suru-indep":
			// The template ja-suru-indep includes the stem
			// because it is irregular.
			formInfl = suffix;
			pronInfl = suffix;
			// Turn the pure-kana forms into forms containing kanji.
			if ("為る".equals(form)) {
				// There seems to be only one reading for the
				// imperfective inflection of the kanji version.
				if (Inflection.IMPERFECTIVE.equals(inflection) || Inflection.IMPERFECTIVE3.equals(inflection)) {
					return;
				}
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
			if (form.endsWith("来る")) {
				// Keep the kanji character.
				formInfl = form.substring(0, form.length() - 1) + suffix.substring(1);
			} else {
				formInfl = form.substring(0, pron.length() - 2) + suffix;
			}
			break;
		case "suru-i-ku":
		case "suru-tsu":
		case "suru":
		case "zuru":
			// Remove the final する/ずる to get the root/stem.
			formInfl = form.substring(0, form.length() - 2) + suffix;
			pronInfl = pron.substring(0, pron.length() - 2) + suffix;
			break;
		case "na":
		case "nari":
		case "tari":
			// Nothing to remove.
			formInfl = form + suffix;
			pronInfl = pron + suffix;
			break;
		default:
			// Remove the final syllable to get the root/stem.
			formInfl = form.substring(0, form.length() - 1) + suffix;
			pronInfl = pron.substring(0, pron.length() - 1) + suffix;
		}

		InflectedToken inflTok = new InflectedToken(tok, formInfl, pronInfl, inflection);
		tok.addInflectedForm(inflTok);
		addToken(inflTok);
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
		for (Entry<String, List<Entry<Inflection, String>>> entry : inflections.entrySet()) {
			logger.fine(entry.getKey() + "-->" + entry.getValue());
		}
	}

	private void setUpTemplate(String filename, InputStream is) {
		// We use a list to retain the order of the inflections, as given in the
		// templates. This makes the inflection tables in the UI look neater.
		List<Entry<Inflection, String>> inflectionParadigm = new ArrayList<>();
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
					inflectionParadigm.add(new SimpleEntry<Inflection, String>(vf, ending));
				} catch (IllegalArgumentException e) {
					if (!line.contains("include") && !line.contains("lemma") && !line.contains("kana")
							&& (!line.contains("note")) && (!line.equals("suru=y"))) {
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

		// Some formal inflections are missing from a lot of the
		// verb inflection table templates.
		// If the template includes a regular "formal" inflection...
		String formalSuffix = inflectionParadigm.stream().filter(entry -> FORMAL.equals(entry.getKey()))
				.map(Entry::getValue).filter(suffix -> suffix.endsWith("ます")).findAny().orElse(null);
		if (formalSuffix != null) {
			// ...but no "formal negative" inflection...
			boolean containsFormalNeg = inflectionParadigm.stream()
					.anyMatch(entry -> FORMAL_NEGATIVE.equals(entry.getKey()));
			if (!containsFormalNeg) {
				// ...use the formal affirmative suffix
				// to infer and add the formal negative one
				String suffix = formalSuffix.replace("ます", "ません");
				inflectionParadigm.add(new SimpleEntry<Inflection, String>(FORMAL_NEGATIVE, suffix));
			}

			// Similarly for "formal perfective"...
			boolean containsFormalPerf = inflectionParadigm.stream()
					.anyMatch(entry -> FORMAL_PERFECTIVE.equals(entry.getKey()) || FORMAL_PAST.equals(entry.getKey()));
			if (!containsFormalPerf) {
				String suffix = formalSuffix.replace("ます", "ました");
				inflectionParadigm.add(new SimpleEntry<Inflection, String>(FORMAL_PERFECTIVE, suffix));
			}
			// ...and "formal negative perfective".
			boolean containsFormalNegPerf = inflectionParadigm.stream()
					.anyMatch(entry -> FORMAL_NEGATIVE_PERFECTIVE.equals(entry.getKey())
							|| FORMAL_NEGATIVE_PAST.equals(entry.getKey()));
			if (!containsFormalNegPerf) {
				String suffix = formalSuffix.replace("ます", "ませんでした");
				inflectionParadigm.add(new SimpleEntry<Inflection, String>(FORMAL_NEGATIVE_PERFECTIVE, suffix));
			}
		}

		inflections.put(getFileName(filename), inflectionParadigm);
	}

	private String getFileName(String filename) {
		return new File(filename).getName().replaceAll("(ja-)|(\\.txt)", "");
	}

}

package de.ws1718.ismla.JapaneseHelper.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private static final String RESOURCES_PATH = "src/main/webapp/WEB-INF/";

	private static final String DICTIONARY_DUMP_PATH = RESOURCES_PATH + "dictionary/";
	private static final String DICTIONARY_GENERATED_PATH = RESOURCES_PATH + "dictionary-full/";
	private static final String INFLECTION_TEMPLATES_PATH = RESOURCES_PATH + "inflection-templates/";
	private Map<String, Map<Inflection, String>> inflections;
	private Set<Token> tokens = new HashSet<>();

	public static void main(String[] args) {
		WiktionaryPreprocessor wp = new WiktionaryPreprocessor();
		wp.setUpInflectionTemplates();
		wp.readTokens();
		wp.printFullDictionary();
	}

	private List<File> getFilesInDir(String directory) {
		List<File> files = new ArrayList<>();
		try (Stream<Path> dir = Files.walk(Paths.get(directory))) {
			files = dir.filter(Files::isRegularFile).map(file -> file.toFile()).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return files;
	}

	private void readTokens() {
		List<File> dictionaryFiles = getFilesInDir(DICTIONARY_DUMP_PATH);
		for (File file : dictionaryFiles) {
			readTokenFile(file);
		}
	}

	private void readTokenFile(File file) {
		String line;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
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
				tokens.add(tok);
				if (tok.isPredicate()) {
					switch (tok.getInflectionParadigm()) {
					case "tari":
					case "?":
						// TODO actually deal with these cases
						continue lines;
					// case "kuru":
					// tok = new Token(form, pronunciation, "V[kuru]",
					// translation);
					// break;
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
							// the pronunciation is listed as "suffix",
							// so we re-use the kana from the form instead
							tok = new Token(form, form, "V[beshi]", translation);
							break;
						default:
							// TODO add other cases instead
							continue lines;
						}
						break;
					case "suru":
						// remove the final "する" from the Wiktionary entries
						tok = new Token(form.substring(0, form.length() - 2),
								pronunciation.substring(0, pronunciation.length() - 2), pos, translation);
						break;
					case "na":
						// the Wiktionary entries end with "(な)"
						// remove the parentheses
						form = form.replaceAll("[\\(\\)]", "");
						pronunciation = pronunciation.replaceAll("[\\(\\)]", "");
						tok = new Token(form, pronunciation, pos, translation);
						tokens.add(tok);
					}
					inflect(tok);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("read tokens");
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
				inflect(tok, suffix.substring(0, parOpen) + suffix.substring(parClose + 1), infl.toString(),
						inflectionName);
				// with optional kana
				inflect(tok, suffix.substring(0, parOpen) + suffix.substring(parOpen + 1, parClose)
						+ suffix.substring(parClose + 1), infl.toString(), inflectionName);
			} else {
				// regular entries
				inflect(tok, suffix, infl.toString(), inflectionName);
			}
			// TODO also incl. inflection paradigm?
		}
	}

	private void inflect(Token tok, String suffix, String inflection, String inflectionName) {
		String form = tok.getForm();
		String pron = tok.getPronunciation();
		String formInfl = "";
		String pronInfl = "";

		switch (inflectionName) {
		// the templates ja-aru and ja-suru-indep include the stem
		// since it is irregular for those verbs
		case "aru":
			if ("有る".equals(form)) {
				formInfl = suffix;
			} else {
				formInfl = aruKanjiToKana(suffix);
			}
			pronInfl = aruKanjiToKana(suffix);
			break;
		case "suru-indep":
			// the する inflection does not include kanji,
			// so form and pronunciation are identical
			formInfl = suffix;
			pronInfl = suffix;
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
		default:
			// remove the final syllable to get the stem
			formInfl = form.substring(0, form.length() - 1) + suffix;
			pronInfl = pron.substring(0, pron.length() - 1) + suffix;
		}

		formInfl = removeWhiteSpace(formInfl);
		pronInfl = removeWhiteSpace(pronInfl);
		tokens.add(new InflectedToken(tok, formInfl, pronInfl, inflection));
	}

	private String removeWhiteSpace(String word) {
		return word.replaceAll("\\s+", "");
	}

	private String aruKanjiToKana(String word) {
		return word.replace('有', 'あ').replace('無', 'な');
	}

	private void setUpInflectionTemplates() {
		inflections = new HashMap<>();
		List<File> inflectionFiles = getFilesInDir(INFLECTION_TEMPLATES_PATH);
		logger.info("Found the following " + inflectionFiles.size() + " inflection templates:");
		logger.info(inflectionFiles.stream().map(file -> file.getName()).collect(Collectors.toList()).toString());
		for (File file : inflectionFiles) {
			setUpTemplate(file);
		}
		logger.fine("Read the following " + inflections.size() + " inflection maps:");
		for (Entry<String, Map<Inflection, String>> entry : inflections.entrySet()) {
			logger.fine(entry.getKey() + "-->" + entry.getValue());
		}
	}

	private void setUpTemplate(File file) {
		Map<Inflection, String> inflectionScheme = new HashMap<>();
		String line;

		try (InputStream stream = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("﻿##") || line.startsWith("##")) {
					// the first version contains control characters
					continue;
				}
				line = line.replaceAll("[->\\|<!]", "");
				String[] fields = line.split("=");
				if (fields.length != 2) {
					continue;
				}
				try {
					Inflection vf = Inflection.valueOf(fields[0].trim().toUpperCase());
					String ending = fields[1].trim().replaceAll("\\. ", "");
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
		inflections.put(getFileName(file), inflectionScheme);
	}

	private String getFileName(File file) {
		return file.getName().replaceAll("(ja-)|(\\.txt)", "");
	}

	private void printFullDictionary() {
		LocalDateTime ldt = LocalDateTime.now();
		int year = ldt.getYear();
		String month = "" + ldt.getMonthValue();
		if (month.length() == 1) {
			month = "0" + month;
		}
		String day = "" + ldt.getDayOfMonth();
		if (day.length() == 1) {
			day = "0" + day;
		}
		String fileName = DICTIONARY_GENERATED_PATH + "dictionary-full-" + year + month + day + ".tsv";
		File file = new File(fileName);
		try (PrintWriter pw = new PrintWriter(file)) {
			pw.println("## Based on data from https://en.wiktionary.org/");
			for (Token tok : tokens) {
				pw.println(tok);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.info("printed the full dictionary");
	}

}

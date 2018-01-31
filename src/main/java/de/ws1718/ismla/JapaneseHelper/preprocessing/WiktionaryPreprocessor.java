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

import de.ws1718.ismla.JapaneseHelper.server.preprocessing.Inflection;
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
		wp.readTokens();
		wp.setUpInflectionTemplates();
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
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("﻿##") || line.startsWith("##")) {
					// the first version contains control characters
					continue;
				}
				String[] fields = line.split("\t");
				if (fields.length < 4) {
					continue;
				}
				Token tok = new Token(fields[0], fields[1], fields[2], fields[3]);
				logger.info(tok.toString());
				tokens.add(tok);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("read tokens");
	}

	private void setUpInflectionTemplates() {
		inflections = new HashMap<>();
		List<File> inflectionFiles = getFilesInDir(INFLECTION_TEMPLATES_PATH);
		logger.info("Found the following " + inflectionFiles.size() + " inflection templates:");
		logger.info(inflectionFiles.stream().map(file -> file.getName()).collect(Collectors.toList()).toString());
		for (File file : inflectionFiles) {
			setUpTemplate(file);
		}
		logger.info("Read the following " + inflections.size() + " inflection maps:");
		for (Entry<String, Map<Inflection, String>> entry : inflections.entrySet()) {
			logger.info(entry.getKey() + "-->" + entry.getValue());
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
		inflections.put(getName(file), inflectionScheme);
	}

	private String getName(File file) {
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
			pw.println("## Source: https://en.wiktionary.org/");
			for (Token tok : tokens) {
				pw.println(tok);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		logger.info("printed the full dictionary");
	}

}

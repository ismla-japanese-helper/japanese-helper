package de.ws1718.ismla.JapaneseHelper.server.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class WiktionaryPreprocessor extends RemoteServiceServlet {

	private static final long serialVersionUID = -4971857652557473025L;
	private static final Logger logger = Logger.getLogger(WiktionaryPreprocessor.class.getSimpleName());

	// TODO make this more flexible + GWT-friendly
	private static final String RESOURCE_PATH = "src/main/webapp/WEB-INF/";
	private static final String DICTIONARY_PATH = RESOURCE_PATH + "dictionary/ja-wiki-preprocessed-20180112.tsv";
	private static final String INFLECTION_TEMPLATES_PATH = RESOURCE_PATH + "inflection-templates";
	private Map<String, Map<Inflection, String>> inflections;

	private void readFile() {
		String line;
		// try (InputStream stream =
		// getServletContext().getResourceAsStream(DICTIONARY_PATH);
		try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream(DICTIONARY_PATH);
				BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			while ((line = br.readLine()) != null) {
				line = line.trim();
				String[] fields = line.split("\t");
				if (fields.length < 4) {
					continue;
				}
				Token tok = new Token(fields[0], fields[1], fields[2], fields[3]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setUpInflectionTemplates() {
		inflections = new HashMap<>();
		List<File> inflectionFiles = new ArrayList<>();
		// TODO can we make this work with
		// getServletContext().getResourceAsStream ?
		try (Stream<Path> dir = Files.walk(Paths.get(INFLECTION_TEMPLATES_PATH))) {
			inflectionFiles = dir.filter(Files::isRegularFile).map(file -> file.toFile()).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		// TODO can we make this work with
		// getServletContext().getResourceAsStream ?
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

	public static void main(String[] args) {
		WiktionaryPreprocessor wp = new WiktionaryPreprocessor();
		wp.setUpInflectionTemplates();
	}

}

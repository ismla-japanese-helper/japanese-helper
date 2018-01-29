package de.ws1718.ismla.JapaneseHelper.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.JapaneseHelper.client.GreetingService;
import de.ws1718.ismla.JapaneseHelper.server.preprocessing.Inflection;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {
	private static final Logger logger = Logger.getLogger(GreetingServiceImpl.class.getSimpleName());

	private static final String DICTIONARY_PATH = "/WEB-INF/dictionary/ja-wiki-preprocessed-20180112.tsv";
	private static final String INFLECTION_TEMPLATES_PATH = "/WEB-INF/inflection-templates";
	private Map<String, Map<Inflection, String>> inflections;

	public String greetServer(String input) throws IllegalArgumentException {
		readTokens();
		setUpInflectionTemplates();
		
		return "done";
	}

	private void readTokens() {
		String line;
		try (InputStream stream = getServletContext().getResourceAsStream(DICTIONARY_PATH);
				BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
			while ((line = br.readLine()) != null) {
				logger.info(line);
				line = line.trim();
				String[] fields = line.split("\t");
				if (fields.length < 4) {
					continue;
				}
				Token tok = new Token(fields[0], fields[1], fields[2], fields[3]);
				logger.info(tok.toString());
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
		Set<String> inflectionFiles = getServletContext().getResourcePaths(INFLECTION_TEMPLATES_PATH);
		for (String file : inflectionFiles) {
			setUpTemplate(file);
		}
		logger.info("Read the following " + inflections.size() + " inflection maps:");
		for (Entry<String, Map<Inflection, String>> entry : inflections.entrySet()) {
			logger.info(entry.getKey() + "-->" + entry.getValue());
		}
	}

	private void setUpTemplate(String file) {
		Map<Inflection, String> inflectionScheme = new HashMap<>();
		String line;

		try (InputStream stream = getServletContext().getResourceAsStream(file);
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
					Inflection infl = Inflection.valueOf(fields[0].trim().toUpperCase());
					String ending = fields[1].trim().replaceAll("\\. ", "");
					inflectionScheme.put(infl, ending);
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
		inflections.put(getName(new File(file)), inflectionScheme);
	}

	private String getName(File file) {
		return file.getName().replaceAll("(ja-)|(\\.txt)", "");
	}

}

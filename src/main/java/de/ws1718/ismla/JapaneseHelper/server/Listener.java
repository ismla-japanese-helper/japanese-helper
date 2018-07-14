package de.ws1718.ismla.JapaneseHelper.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ListMultimap;
import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;

import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class Listener {

	public static final String RESOURCES_PATH = "src/main/webapp/WEB-INF/";
	public static final String DIFFICULTY_RATING_PATH = RESOURCES_PATH + "difficulty-rating/joyo-kanji.csv";
	public static final String DICTIONARY_PATH = RESOURCES_PATH + "dictionary/";
	public static final String INFLECTION_TEMPLATES_PATH = RESOURCES_PATH + "inflection-templates/";

	public static void main(String[] args) {
		String tokenizationFile = "";
		int start = -1;
		int stop = -1;
		try {
			tokenizationFile = args[0];
			start = new Integer(args[1]);
			stop = new Integer(args[2]);
		} catch (Exception e) {
			System.err.println("Expects three arguments:\n" + "- the relative path to the file\n"
					+ "- the index of the line for starting tokenization (inclusive)\n"
					+ "- the index of the line for stopping tokenization (exclusive)");
			System.exit(1);
		}

		HashMap<String, String> difficultyRatings = readDifficultyRatings();
		ListMultimap<String, Token> tokenMap = readTokens(difficultyRatings);
		
		LookupServiceImpl tokenizer = new LookupServiceImpl(tokenMap, tokenizationFile, start, stop);
		tokenizer.tokenizeFile();
	}

	/**
	 * Reads the tokens from the Wiktionary dump and generates inflected forms.
	 * 
	 * @param sce
	 *            the ServletContextEvent
	 * @param difficultyRatings
	 *            a map containing jouyou kanji difficulty ratings
	 * @return a ListMultimap from token forms to Token instances
	 */
	private static ListMultimap<String, Token> readTokens(HashMap<String, String> difficultyRatings) {
		List<String> inflectionFiles = new ArrayList<>();
		List<InputStream> inflectionStreams = new ArrayList<>();
		for (File file : new File(INFLECTION_TEMPLATES_PATH).listFiles()) {
			inflectionFiles.add(file.getName());
			try {
				inflectionStreams.add(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		List<InputStream> dictionaryStreams = new ArrayList<>();
		for (File file : new File(DICTIONARY_PATH).listFiles()) {
			try {
				dictionaryStreams.add(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		WiktionaryPreprocessor wp = new WiktionaryPreprocessor(inflectionFiles, inflectionStreams, dictionaryStreams,
				difficultyRatings);
		return wp.getTokens();
	}

	/**
	 * Reads the file containing jouyou kanji difficulty information and
	 * converts it into a map.
	 * 
	 * @param difficultyRatingStream
	 *            the input stream of the jouyou kanji file
	 * @return the map from kanji to their difficulty ratings
	 */
	// public for testing
	public static HashMap<String, String> readDifficultyRatings() {
		InputStream difficultyRatingStream;
		HashMap<String, String> difficultyRatings = new HashMap<>();
		try {
			difficultyRatingStream = new FileInputStream(new File(DIFFICULTY_RATING_PATH));

			try (InputStreamReader isr = new InputStreamReader(difficultyRatingStream);
					CSVReader reader = new CSVReader(isr)) {
				CSVIterator iterator = new CSVIterator(reader);

				for (CSVIterator it = iterator; it.hasNext();) {
					String[] line = it.next();
					String character = line[1];
					String difficulty = line[5];
					difficultyRatings.put(character, difficulty);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (difficultyRatingStream != null) {
					try {
						difficultyRatingStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		return difficultyRatings;
	}

}

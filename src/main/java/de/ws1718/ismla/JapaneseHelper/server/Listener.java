package de.ws1718.ismla.JapaneseHelper.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.ListMultimap;
import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;

import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class Listener {

	public static final String RESOURCES_PATH = "/"; // for the JAR
	public static final String DIFFICULTY_RATING_PATH = RESOURCES_PATH + "difficulty-rating/joyo-kanji.csv";
	public static final String DICTIONARY_PATH = RESOURCES_PATH + "dictionary/ja-wiki-preprocessed-20180309.tsv";
	public static final String INFLECTION_TEMPLATES_PATH = RESOURCES_PATH + "inflection-templates/";
	private static final List<String> INFLECTION_FILES = new ArrayList<String>(
			Arrays.asList("ja-aru.txt", "ja-beshi.txt", "ja-da.txt", "ja-dekiru.txt", "ja-go-bu.txt", "ja-go-gu.txt",
					"ja-go-ku.txt", "ja-go-mu.txt", "ja-go-nu.txt", "ja-go-ou.txt", "ja-go-ru.txt", "ja-go-su.txt",
					"ja-go-tsu.txt", "ja-go-u.txt", "ja-honorific.txt", "ja-i.txt", "ja-ichi.txt", "ja-iku.txt",
					"ja-kureru.txt", "ja-kuru.txt", "ja-na.txt", "ja-nari.txt", "ja-suru-i-ku.txt", "ja-suru-indep.txt",
					"ja-suru-tsu.txt", "ja-suru.txt", "ja-tari.txt", "ja-ya.txt", "ja-zuru.txt"));

	public Listener(String tokenizationFile, int start, int stop) {
		InputStream difficultyRatingsStream = Listener.class.getResourceAsStream(DIFFICULTY_RATING_PATH);
		HashMap<String, String> difficultyRatings = readDifficultyRatings(difficultyRatingsStream);

		System.out.println(Listener.class.getResourceAsStream(INFLECTION_TEMPLATES_PATH) != null);

	}

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
		new Listener(tokenizationFile, start, stop);
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
	private static HashMap<String, String> readDifficultyRatings(InputStream difficultyRatingStream) {
		HashMap<String, String> difficultyRatings = new HashMap<>();
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

		return difficultyRatings;
	}

}

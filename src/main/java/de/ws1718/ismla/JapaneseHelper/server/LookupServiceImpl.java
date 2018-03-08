package de.ws1718.ismla.JapaneseHelper.server;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.atilika.kuromoji.ipadic.Tokenizer;
import com.google.common.collect.Multimap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.JapaneseHelper.client.LookupService;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class LookupServiceImpl extends RemoteServiceServlet implements LookupService {
	private static final long serialVersionUID = 568570423376066244L;

	private static final Logger logger = Logger.getLogger(LookupServiceImpl.class.getSimpleName());

	// requires the file inside the directory to have been generated by
	// WiktionaryPreprocessor
	public static final String DICTIONARY_PATH = "/WEB-INF/dictionary/";
	public static final String INFLECTION_TEMPLATES_PATH = "/WEB-INF/inflection-templates/";

	private Multimap<String, Token> tokens;

	public List<Token> lookup(String sentence) {
		readTokens();
		Tokenizer tokenizer = new Tokenizer();
		// This is the Token defined by the Kuromoji parser.
		List<com.atilika.kuromoji.ipadic.Token> tokens = tokenizer.tokenize(sentence.trim());

		// This is the Token defined by us.
		List<Token> results = new ArrayList<>();
		// Deal with this part of the code later.
		for (com.atilika.kuromoji.ipadic.Token t : tokens) {
			Token convertedToken = convertToken(t);
			results.add(convertedToken);
		}

		return results;
	}

	private Token convertToken(com.atilika.kuromoji.ipadic.Token t) {
		logger.info(t.toString());
		String form = t.getSurface();
		// TODO use the Kuromoji-Wiktionary POS tag mapping here
		// (and use the other POS levels for that mapping)
		String pos = t.getPartOfSpeechLevel1();
		// TODO we could use t.getPronunciation(), t.getReading()
		String pron = t.getPronunciation();
		logger.info(form + ", " + pos + ", " + pron);

		Collection<Token> matches = tokens.get(form);
		for (Token tok : matches) {
			logger.info(tok.toString());
		}
		if (matches == null || matches.isEmpty()) {
			// TODO transform pron, pos
			return new Token(form, pron, pos, "1) [out-of-vocabulary]");
		}

		ArrayList<Token> matchList = new ArrayList<>(matches);

		// TODO sort list
		return matchList.get(0);
	}

	private void readTokens() {
		List<String> inflectionFiles = new ArrayList<String>(
				getServletContext().getResourcePaths(INFLECTION_TEMPLATES_PATH));
		List<InputStream> inflectionStreams = new ArrayList<>();
		for (String file : inflectionFiles) {
			inflectionStreams.add(getServletContext().getResourceAsStream(file));
		}
		List<String> dictionaryFiles = new ArrayList<String>(getServletContext().getResourcePaths(DICTIONARY_PATH));
		List<InputStream> dictionaryStreams = new ArrayList<>();
		for (String file : dictionaryFiles) {
			dictionaryStreams.add(getServletContext().getResourceAsStream(file));
		}
		WiktionaryPreprocessor wp = new WiktionaryPreprocessor(inflectionFiles, inflectionStreams, dictionaryStreams);
		tokens = wp.getTokens();
	}

}

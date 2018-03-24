package de.ws1718.ismla.JapaneseHelper.server;

import com.google.common.collect.ListMultimap;
import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Doesn't work with GWT it seems. Had to manually add it to web.xml
//@WebListener()
public class Listener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    // requires the file inside the directory to have been generated by
    // WiktionaryPreprocessor
    public static final String DIFFICULTY_RATING_PATH = "/WEB-INF/difficulty-rating/joyo-kanji.csv";
    public static final String DICTIONARY_PATH = "/WEB-INF/dictionary/";
    public static final String INFLECTION_TEMPLATES_PATH = "/WEB-INF/inflection-templates/";

    // Public constructor is required by servlet spec
    public Listener() {
    }

    // -------------------------------------------------------
    // ServletContextListener implementation
    // -------------------------------------------------------
    public void contextInitialized(ServletContextEvent sce) {
      /* This method is called when the servlet context is
         initialized(when the Web application is deployed). 
         You can initialize servlet context related data here.
      */

        // First read the file storing kanji difficulty ratings.
        InputStream difficultyRatingStream = sce.getServletContext().getResourceAsStream(DIFFICULTY_RATING_PATH);
        HashMap<String, String> difficultyRatings = readDifficultyRatings(difficultyRatingStream);

        // Then the WP dump.
        ListMultimap<String, Token> tokenMap = readTokens(sce, difficultyRatings);
        sce.getServletContext().setAttribute("tokenMap", tokenMap);
    }


    public void contextDestroyed(ServletContextEvent sce) {
      /* This method is invoked when the Servlet Context 
         (the Web application) is undeployed or 
         Application Server shuts down.
      */
    }

    // -------------------------------------------------------
    // HttpSessionListener implementation
    // -------------------------------------------------------
    public void sessionCreated(HttpSessionEvent se) {
        /* Session is created. */
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        /* Session is destroyed. */
    }

    // -------------------------------------------------------
    // HttpSessionAttributeListener implementation
    // -------------------------------------------------------

    public void attributeAdded(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute 
         is added to a session.
      */
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
      /* This method is called when an attribute
         is removed from a session.
      */
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
      /* This method is invoked when an attibute
         is replaced in a session.
      */
    }

    private ListMultimap<String, Token> readTokens(ServletContextEvent sce, HashMap<String, String> difficultyRatings) {
        List<String> inflectionFiles = new ArrayList<String>(
                sce.getServletContext().getResourcePaths(INFLECTION_TEMPLATES_PATH));
        List<InputStream> inflectionStreams = new ArrayList<>();
        for (String file : inflectionFiles) {
            inflectionStreams.add(sce.getServletContext().getResourceAsStream(file));
        }
        List<String> dictionaryFiles = new ArrayList<String>(sce.getServletContext().getResourcePaths(DICTIONARY_PATH));
        List<InputStream> dictionaryStreams = new ArrayList<>();
        for (String file : dictionaryFiles) {
            dictionaryStreams.add(sce.getServletContext().getResourceAsStream(file));
        }
        WiktionaryPreprocessor wp = new WiktionaryPreprocessor(inflectionFiles, inflectionStreams, dictionaryStreams, difficultyRatings);
        return wp.getTokens();
    }

    public static HashMap<String,String> readDifficultyRatings(InputStream difficultyRatingStream) {
        HashMap<String, String> difficultyRatings = new HashMap<>();

        CSVIterator iterator = null;
        try {
            iterator = new CSVIterator(new CSVReader(new InputStreamReader(difficultyRatingStream)));

            // Why does it say that forEach is inapplicable? Because it's now an InputStreamReader, not FileReader?
            for (CSVIterator it = iterator; it.hasNext(); ) {
                String[] line = it.next();
                String character = line[1];
                String difficulty = line[5];
                difficultyRatings.put(character, difficulty);
            }

            difficultyRatingStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return difficultyRatings;
    }
}

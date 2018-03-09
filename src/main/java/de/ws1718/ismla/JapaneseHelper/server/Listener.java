package de.ws1718.ismla.JapaneseHelper.server;

import com.google.common.collect.ListMultimap;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionBindingEvent;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// Doesn't work with GWT it seems. Had to manually add it to web.xml
//@WebListener()
public class Listener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    private static final String DICTIONARY_PATH = "/WEB-INF/dictionary/";
    private static final String INFLECTION_TEMPLATES_PATH = "/WEB-INF/inflection-templates/";

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
        ListMultimap<String, Token> tokenMap = readTokens(sce);

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

    private ListMultimap<String, Token> readTokens(ServletContextEvent sce) {
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
        WiktionaryPreprocessor wp = new WiktionaryPreprocessor(inflectionFiles, inflectionStreams, dictionaryStreams);
        return wp.getTokens();
    }
}

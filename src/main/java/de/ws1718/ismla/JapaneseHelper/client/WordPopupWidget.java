package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.List;

public class WordPopupWidget extends Composite {
	@UiField
	HTMLPanel glossesContainer;

	interface WordPopupWidgetUiBinder extends UiBinder<HTMLPanel, WordPopupWidget> {
	}

	private static WordPopupWidgetUiBinder ourUiBinder = GWT.create(WordPopupWidgetUiBinder.class);

	/**
	 * A widget for displaying additional translations and alternative token
	 * matches in a pop-up.
	 * 
	 * @param tokens
	 *            the token and any alternative token matches
	 */
	public WordPopupWidget(List<Token> tokens) {
		initWidget(ourUiBinder.createAndBindUi(this));
		glossesContainer.addStyleName("container");
		HTML glossesList = generateGlossesList(tokens);
		glossesContainer.add(glossesList);
	}

	/**
	 * Turns the translation(s) of the token(s) into an HTML list.
	 * 
	 * @param tokens
	 *            the token and any alternative token matches
	 * @return the HTML list
	 */
	private HTML generateGlossesList(List<Token> tokens) {
		Token firstToken = tokens.get(0);
		String html = "";
		// First append the original word to deal with the situation where the word is blocked by the popup.
		String firstEntry = firstToken.getPrettyPos() + ", " + firstToken.getPronunciation();
		if (firstToken instanceof InflectedToken) {
			firstEntry += ", " + ((InflectedToken) firstToken).getInflection();
		}
		html += "<li class='list-group-item'><b>" + firstEntry + "</b></li>";
		html += "<ul class='list-group'>";
		List<String> glosses = tokens.get(0).getTranslations();
		for (String gloss : glosses) {
			html += "<li class='list-group-item'>" + gloss + "</li>";
		}

		if (tokens.size() > 1) {
			for (int index = 1; index < tokens.size(); index++) {
				Token curToken = tokens.get(index);
				String entry = curToken.getPrettyPos() + ", " + curToken.getPronunciation();
				if (curToken instanceof InflectedToken) {
					entry += ", " + ((InflectedToken) curToken).getInflection();
				}
				html += "<li class='list-group-item'><b>Potential alternative (" + entry + ") :</b></li>";
				glosses = curToken.getTranslations();
				for (String gloss : glosses) {
					html += "<li class='list-group-item'>" + gloss + "</li>";
				}
			}
		}

		html += "</ul>";

		return new HTML(html);
	}
}

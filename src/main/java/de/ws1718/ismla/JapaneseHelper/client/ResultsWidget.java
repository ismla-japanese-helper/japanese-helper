package de.ws1718.ismla.JapaneseHelper.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import de.ws1718.ismla.JapaneseHelper.shared.InflectableToken;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class ResultsWidget extends Composite {
	@UiField
	HTMLPanel resultsContainer;

	private static ResultsWidgetUiBinder uiBinder = GWT.create(ResultsWidgetUiBinder.class);

	interface ResultsWidgetUiBinder extends UiBinder<Widget, ResultsWidget> {
	}

	/**
	 * A widget for displaying details about the tokens contained in a sentence.
	 *
	 * @param sentence
	 *            the sentence
	 */
	public ResultsWidget(List<ArrayList<Token>> sentence) {
		initWidget(uiBinder.createAndBindUi(this));
		resultsContainer.addStyleName("container");
		HTMLPanel results = generateResultsTable(sentence);

		// The order of the anchors should correspond to the order
		// in which the tokens are stored in the sentence.
		NodeList<Element> anchors = results.getElement().getElementsByTagName("a");
		int anchorIndex = 0;

		// This should work, though a bit ugly.
		// Now we have one more anchor for the inflection table.
		for (int i = 0; i < sentence.size(); i++) {
			final int finalI = i;
			List<Token> currentTokens = sentence.get(finalI);
			Token firstToken = currentTokens.get(0);

			// First the popup about glosses.
			Element glossesAnchor = anchors.getItem(anchorIndex);
			Anchor glossAnchorWithLink = new Anchor(firstToken.getTranslations().get(0));
			glossAnchorWithLink.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					new Popup(new WordPopupWidget(currentTokens)).center();
				}
			});
			results.addAndReplaceElement(glossAnchorWithLink, glossesAnchor);

			// If possible, add an inflection table widget.
			if (firstToken instanceof InflectedToken) {
				InflectableToken lemmaToken = ((InflectedToken) firstToken).getLemmaToken();
				anchorIndex++;
				Element inflectionTableAnchor = anchors.getItem(anchorIndex);
				Anchor inflectionTableAnchorWithLink = new Anchor(
						((InflectedToken) firstToken).getLemmaAndInflectionInformation());
				inflectionTableAnchorWithLink.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						new Popup(new InflectionTableWidget(lemmaToken)).center();
					}
				});
				results.addAndReplaceElement(inflectionTableAnchorWithLink, inflectionTableAnchor);
			}

			anchorIndex++;
		}

		resultsContainer.add(results);
	}

	/**
	 * Generates a table that gives details about the tokens contained in a
	 * sentence.
	 *
	 * @param sentence
	 *            the sentence
	 * @return the table
	 */
	private HTMLPanel generateResultsTable(List<ArrayList<Token>> sentence) {
		String html = "";
		// This is the outer row for all the columns.
		html += "<div class='row'>";

		for (List<Token> list : sentence) {
			html += generateOneWord(list.get(0));
		}

		html += "</div>";
		return new HTMLPanel(html);
	}

	/**
	 * Generates a table (1 column, 5 rows) that contains details about the
	 * given token.
	 *
	 * @param t
	 *            the token
	 * @return the table in HTML format
	 */
	private String generateOneWord(Token t) {
		String representation = "";
		representation += "<div class='col-xs-4 col-md-3 col-lg-2 mb-3'>";
		// Needed to create a nested row within the outer column.
		representation += "<div class='row'>";

		// Each row of the output table is actually achieved via a full-width
		// "column" in Bootstrap. In this way, the next "column" will be
		// automatically pushed to the next row in the final display.
		representation += "<div class='col-12'>" + t.getForm() + "</div>";
		representation += "<div class='col-12'>" + t.getPronunciation() + "</div>";
		// Manually added <a> here.
		representation += "<div class='col-12' title='Click for full list of translations'><a>"
				+ t.getTranslations().get(0) + "</a></div>";
		representation += "<div class='col-12'>" + t.getPrettyPos() + "</div>";

		if (t instanceof InflectedToken) {
			String inflectionInfo = ((InflectedToken) t).getLemmaAndInflectionInformation();
			representation += "<div class='col-12' title='Click for full list of inflections'><a>" + inflectionInfo
					+ "</a></div>";
		} else {
			representation += "<div class='col-12'>" + "*" + "</div>";
		}
		representation += "<div class='col-12'>" + t.getDifficultyRating() + "</div>";

		representation += "</div>";
		representation += "</div>";

		return representation;
	}

	private static class Popup extends PopupPanel {
		public Popup(Widget popupWidget) {
			// "autoHide: true" means that if the user clicks on anywhere
			// outside of the popup, it will automatically close.
			super(true);
			setWidget(popupWidget);
		}
	}

}

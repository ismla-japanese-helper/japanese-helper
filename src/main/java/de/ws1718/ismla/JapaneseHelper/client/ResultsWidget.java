package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;

import java.util.ArrayList;
import java.util.List;

import de.ws1718.ismla.JapaneseHelper.shared.InflectableToken;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;


public class ResultsWidget extends Composite {
	@UiField
	HTMLPanel resultsContainer;

	@UiField
	Button clearButton;

	private static ResultsWidgetUiBinder uiBinder = GWT.create(ResultsWidgetUiBinder.class);

	interface ResultsWidgetUiBinder extends UiBinder<Widget, ResultsWidget> {
	}

	@UiHandler("clearButton")
	void onClick(ClickEvent e) {
		RootPanel.get("resultsContainer").clear();
		RootPanel.get("inputContainer").clear();
		RootPanel.get("inputContainer").add(new SentenceInputWidget());
	}

	public ResultsWidget(List<ArrayList<Token>> sentence) {
		initWidget(uiBinder.createAndBindUi(this));
		clearButton.setText("Clear and enter new text");
		// Or maybe I can indeed append a class to this one after all. What's the issue with that anyways.
		resultsContainer.addStyleName("container");
		HTMLPanel results = generateResultsTable(sentence);

		// See https://stackoverflow.com/questions/7465988/how-to-capture-a-click-event-on-a-link-inside-a-html-widget-in-gwt
		// OK. So in our case the order of the anchors should correspond to the order in which the tokens are stored in the sentence.
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

			if (firstToken instanceof InflectedToken) {
				InflectableToken lemmaToken = ((InflectedToken) firstToken).getLemmaToken();
				anchorIndex++;
				Element inflectionTableAnchor = anchors.getItem(anchorIndex);
				Anchor inflectionTableAnchorWithLink = new Anchor(((InflectedToken) firstToken).getInflectionInformation());
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

	// Not sure if this is the most elegant way to go. But we're generating HTML anyways, why bother with clumsy GWT classes?
	private HTMLPanel generateResultsTable(List<ArrayList<Token>> sentence) {
		String html = "";
		Boolean multiEntries = false;
		// This is the outer row for all the columns.
		html += "<div class='row'>";

		for (List<Token> list : sentence) {
            html += generateOneWord(list.get(0));
		}

		html += "</div>";
		return new HTMLPanel(html);
	}

	private String generateOneWord(Token t) {
		String representation = "";
		representation += "<div class='col-xs-4 col-md-3 col-lg-2 mb-3'>";
		// Needed to create a nested row within the outer column.
		representation += "<div class='row'>";

		representation += "<div class='col-12'>" + t.getForm() + "</div>";
		representation += "<div class='col-12'>" + t.getPronunciation() + "</div>";
		// Manually added <a> here.
		representation += "<div class='col-12' title='Click for full list of glosses'><a>" + t.getTranslations().get(0) + "</a></div>";
		representation += "<div class='col-12'>" + t.getPrettyPos() + "</div>";

		if (t instanceof InflectedToken) {
			String inflectionInfo = ((InflectedToken) t).getInflectionInformation();
			representation += "<div class='col-12' title='Click for full list of inflections'><a>" + inflectionInfo + "</a></div>";
		} else {
			representation += "<div class='col-12'>" + "*" + "</div>";
		}

		representation += "</div>";
		representation += "</div>";

		return representation;
	}


	private static class Popup extends PopupPanel {
		public Popup(Widget popupWidget) {
			// "autoHide: true" means that if the user clicks on anywhere outside of the popup, it will automatically close.
			super(true);
			setWidget(popupWidget);
		}
	}

}

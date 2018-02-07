package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.List;

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
		RootPanel.get("inputContainer").add(new SentenceInputWidget());
	}

	public ResultsWidget(List<Token> sentence) {
		initWidget(uiBinder.createAndBindUi(this));
		clearButton.setText("Enter new text");
		// Or maybe I can indeed append a class to this one after all. What's the issue with that anyways.
		resultsContainer.addStyleName("container");
		HTML results = generateResultsTable(sentence);
		resultsContainer.add(results);
	}

	// Not sure if this is the most elegant way to go. But we're generating HTML anyways, why bother with clumsy GWT classes?
	private HTML generateResultsTable(List<Token> sentence) {
		String html = "";
		// This is the outer row for all the columns.
		html += "<div class='row'>";

		for (Token t : sentence) {
			html += generateOneWord(t);
		}

		html += "</div>";
		return new HTML(html);
	}

	private String generateOneWord(Token t) {
		String representation = "";
		representation += "<div class='col-xs-6 col-md-4 col-lg-3 mb-3'>";
		// Needed to create a nested row within the outer column.
		representation += "<div class='row'>";

		representation += "<div class='col-12'>" + t.getForm() + "</div>";
		representation += "<div class='col-12'>" + t.getPronunciation() + "</div>";
		representation += "<div class='col-12'>" + t.getTranslation() + "</div>";
		representation += "<div class='col-12'>" + t.getPos() + "</div>";

		representation += "</div>";
		representation += "</div>";

		return representation;
	}

}

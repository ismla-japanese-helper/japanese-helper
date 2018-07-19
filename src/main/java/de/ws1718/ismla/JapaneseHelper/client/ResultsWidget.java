package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.ArrayList;
import java.util.List;

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
		resultsContainer.getElement().addClassName("resultsTable");

		for (List<Token> tokens : sentence) {
			if (!tokens.get(0).getPrettyPos().equals("punctuation")) {
				WordContainerWidget wordContainer = new WordContainerWidget(tokens);
				resultsContainer.add(wordContainer);
			}
		}
	}
}

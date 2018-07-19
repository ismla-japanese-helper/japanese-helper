package de.ws1718.ismla.JapaneseHelper.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class SentenceInputWidget extends Composite {
	private final LookupServiceAsync lookupService = LookupService.App.getInstance();

	interface SentenceInputWidgetUiBinder extends UiBinder<Widget, SentenceInputWidget> {
	}

	private static SentenceInputWidgetUiBinder uiBinder = GWT.create(SentenceInputWidgetUiBinder.class);

	@UiField
	TextArea inputField;

	@UiField
	Button submitButton;
	
	@UiField
	Button tokenizeButton;

	/**
	 * A widget where users can enter text and submit it.
	 */
	public SentenceInputWidget() {
		initWidget(uiBinder.createAndBindUi(this));
		submitButton.setText("Submit");
		tokenizeButton.setText("Tokenize Files");
	}

	@UiHandler("submitButton")
	void onClick(ClickEvent e) {
		RootPanel.get("resultsContainer").clear();
		putResults();
	}

	private void putResults() {
		String input = inputField.getText();

		if (input.isEmpty()) {
			Window.alert("Please input the sentence");
			return;
		}

		lookupService.lookup(input, new AsyncCallback<List<ArrayList<Token>>>() {

			@Override
			public void onSuccess(List<ArrayList<Token>> results) {
				ResultsWidget rw = new ResultsWidget(results);

				RootPanel.get("resultsContainer").add(rw);
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
		});
	}
	
	@UiHandler("tokenizeButton")
	void onTokenizeClick(ClickEvent e) {
		lookupService.tokenizeFiles(new AsyncCallback<String>() {

			@Override
			public void onSuccess(String results) {
				Window.alert("Done!");
			}

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}
		});
	}

}

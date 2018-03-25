package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.ArrayList;
import java.util.List;

public class SentenceInputWidget extends Composite {
    private final LookupServiceAsync lookupService = LookupService.App.getInstance();
    interface SentenceInputWidgetUiBinder extends UiBinder<Widget, SentenceInputWidget> {
    }

    private static SentenceInputWidgetUiBinder uiBinder = GWT.create(SentenceInputWidgetUiBinder.class);

    @UiField
    TextArea inputField;

    @UiField
    Button submitButton;

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

    public SentenceInputWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        submitButton.setText("Submit");
    }
}

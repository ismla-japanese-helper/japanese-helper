package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SentenceInputWidget extends Composite {
    interface SentenceInputWidgetUiBinder extends UiBinder<Widget, SentenceInputWidget> {
    }

    private static SentenceInputWidgetUiBinder uiBinder = GWT.create(SentenceInputWidgetUiBinder.class);

    @UiField
    Button submitButton;

    public SentenceInputWidget() {
        initWidget(uiBinder.createAndBindUi(this));
        submitButton.setText("Submit");
    }
}
package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.List;

// It will be fed a list of glosses. It should display all of them in a list.
// What do we show? The full list of glosses definitely one of them. Should we also show the inflection and any special note maybe? I can leave space for those for sure.
// Let me first show the glosses anyways.
public class WordPopupWidget extends Composite {
    @UiField
    HTMLPanel glossesContainer;

    interface WordPopupWidgetUiBinder extends UiBinder<HTMLPanel, WordPopupWidget> {
    }

    private static WordPopupWidgetUiBinder ourUiBinder = GWT.create(WordPopupWidgetUiBinder.class);

    public WordPopupWidget(Token word) {
        initWidget(ourUiBinder.createAndBindUi(this));
        glossesContainer.addStyleName("container");
        HTML glossesList = generateGlossesList(word.getTranslations());
        glossesContainer.add(glossesList);
    }

    private HTML generateGlossesList(List<String> glosses) {
        String html = "";
        html += "<ul class='list-group'>";

        for (String gloss : glosses) {
            html += "<li class='list-group-item'>" + gloss + "</li>";
        }

        html += "</ul>";

        return new HTML(html);
    }
}

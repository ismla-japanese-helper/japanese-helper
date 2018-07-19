package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import de.ws1718.ismla.JapaneseHelper.shared.InflectableToken;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.List;

public class WordContainerWidget extends Composite {
	@UiField
	DivElement form;
	@UiField
	DivElement pronunciation;
	@UiField
	FocusPanel translation;
	@UiField
	DivElement pos;
	@UiField
	FocusPanel inflection;
	@UiField
	DivElement difficulty;

	@UiField
	Style style;

	// ... Why does there have to be so much boilerplate code for such a supposedly simple functionality?
	interface Style extends CssResource {
		@ClassName("result-row")
		String resultRow();

		String clickable();

		String translation();

		String wordContainer();
	}


	interface WordContainerWidgetUiBinder extends UiBinder<HTMLPanel, WordContainerWidget> {
	}

	private static WordContainerWidgetUiBinder ourUiBinder = GWT.create(WordContainerWidgetUiBinder.class);

	public WordContainerWidget(List<Token> tokens) {
		initWidget(ourUiBinder.createAndBindUi(this));

		Token firstToken = tokens.get(0);

		form.setInnerText(firstToken.getForm());
		pronunciation.setInnerText(firstToken.getPronunciation());

		String firstTranslation = firstToken.getTranslations().get(0);
		if (firstTranslation.length() > 50) {
			firstTranslation = firstTranslation.substring(0, 47) + "...";
		}


		// Add the popup anchor if there are multiple meanings.
		if (tokens.size() > 1) {
			translation.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent clickEvent) {
					new Popup(new WordPopupWidget(tokens)).center();
				}
			});
			translation.addStyleName(style.clickable());
			translation.add(new HTMLPanel("<div title=\'click to see all translations\'>" + firstTranslation + "</div>"));
		} else {
			translation.add(new HTMLPanel(firstTranslation));
		}

		if (firstToken instanceof InflectedToken) {
			InflectableToken lemmaToken = ((InflectedToken) firstToken).getLemmaToken();
			inflection.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					new Popup(new InflectionTableWidget(lemmaToken)).center();
				}
			});

			inflection.add(new HTMLPanel("<div title=\'Click to see inflection table.\'>" + ((InflectedToken) firstToken).getLemmaAndInflectionInformation() + "</div>"));
			inflection.addStyleName(style.clickable());
		} else {
			inflection.add(new HTMLPanel("*"));
		}

		difficulty.setInnerText(firstToken.getDifficultyRating());
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
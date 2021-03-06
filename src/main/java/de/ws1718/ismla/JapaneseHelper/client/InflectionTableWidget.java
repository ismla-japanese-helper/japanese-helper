package de.ws1718.ismla.JapaneseHelper.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;

import de.ws1718.ismla.JapaneseHelper.shared.InflectableToken;
import de.ws1718.ismla.JapaneseHelper.shared.InflectedToken;

public class InflectionTableWidget extends Composite {
	@UiField
	HTMLPanel inflectionTableContainer;

	interface InflectionTableWidgetUiBinder extends UiBinder<HTMLPanel, InflectionTableWidget> {
	}

	private static InflectionTableWidgetUiBinder ourUiBinder = GWT.create(InflectionTableWidgetUiBinder.class);

	/**
	 * A widget for displaying an inflection table pop-up.
	 * 
	 * @param token
	 *            the token whose inflected forms will be displayed
	 */
	public InflectionTableWidget(InflectableToken token) {
		initWidget(ourUiBinder.createAndBindUi(this));
		inflectionTableContainer.addStyleName("container");
		HTML inflectionTable = generateInflectionTable(token);
		inflectionTableContainer.add(inflectionTable);
	}

	/**
	 * Creates an HTML table containing the token's possible inflections.
	 * 
	 * @param token
	 *            the base token
	 * @return the HTML table
	 */
	private HTML generateInflectionTable(InflectableToken token) {
		String html = "";
		html += "<table class='table'>";
		html += "<thead><tr><th scope='col'>#</th><th scope='col'>Form</th><th scope='col'>Inflection</th></tr></thead>";
		html += "<tbody>";

		html += "<tr><th scope='row'>0</th>" + "<td>" + token.getForm() + "</td>" + "<td>" + "Base form" + "</td>"
				+ "</tr>";

		ArrayList<InflectedToken> inflectedForms = token.getInflectedForms();

		for (int i = 0; i < inflectedForms.size(); i++) {
			html += "<tr><th scope='row'>" + (i + 1) + "</th>" + "<td>" + inflectedForms.get(i).getForm() + "</td>"
					+ "<td>" + inflectedForms.get(i).getInflectionInformation() + "</td>" + "</tr>";
		}

		html += "</tbody>" + "</table>";

		return new HTML(html);
	}
}
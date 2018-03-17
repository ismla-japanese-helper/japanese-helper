package de.ws1718.ismla.JapaneseHelper.shared;

import java.util.ArrayList;

public class InflectableToken extends Token {

	private static final long serialVersionUID = 5257616244138720043L;
	ArrayList<InflectedToken> inflectedForms;

	public InflectableToken() {
		super();
		inflectedForms = new ArrayList<>();
	}

	public InflectableToken(Token tok) {
		super(tok.getForm(), tok.getPronunciation(), tok.getPos(), tok.getInflectionParadigm(), tok.getTranslations());
		inflectedForms = new ArrayList<>();
	}

	public InflectableToken(String form, String pronunciation, String posAndInflection, String translation) {
		super(form, pronunciation, posAndInflection, translation);
		inflectedForms = new ArrayList<>();
	}

	/**
	 * @return the inflectedForms
	 */
	public ArrayList<InflectedToken> getInflectedForms() {
		return inflectedForms;
	}

	/**
	 * @param inflectedForms
	 *            the inflectedForms to set
	 */
	public void setInflectedForms(ArrayList<InflectedToken> inflectedForms) {
		this.inflectedForms = inflectedForms;
	}

	public void addInflectedForm(InflectedToken tok) {
		inflectedForms.add(tok);
	}

}

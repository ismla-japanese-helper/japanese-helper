package de.ws1718.ismla.JapaneseHelper.shared;

import java.util.ArrayList;

public class InflectableToken extends Token {

	private static final long serialVersionUID = 5257616244138720043L;
	ArrayList<InflectedToken> inflectedForms;

	/**
	 * Constructs a dummy InflectableToken.
	 */
	public InflectableToken() {
		super();
		inflectedForms = new ArrayList<>();
	}

	/**
	 * Constructs an InflectableToken based on a regular {@link Token}.
	 * 
	 * @param tok
	 *            the base token
	 */
	public InflectableToken(Token tok) {
		super(tok.getForm(), tok.getPronunciation(), tok.getPos(), tok.getInflectionParadigm(), tok.getTranslations());
		inflectedForms = new ArrayList<>();
	}

	/**
	 * Constructs an InflectableToken.
	 * 
	 * @param form
	 *            the form (in kanji and kana)
	 * @param pronunciation
	 *            the pronunciation (in kana)
	 * @param posAndInflection
	 *            the POS tag in one of the following formats:
	 *            "pos[inflectionParadigm]" or "pos"
	 * @param translation
	 *            the enumeration of translations, e.g.
	 *            "1) the first meaning 2) the second meaning 3) etc."
	 */
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

	/**
	 * Adds an inflected token to the list of inflection forms.
	 * 
	 * @param tok
	 *            the token
	 */
	public void addInflectedForm(InflectedToken tok) {
		inflectedForms.add(tok);
	}

}

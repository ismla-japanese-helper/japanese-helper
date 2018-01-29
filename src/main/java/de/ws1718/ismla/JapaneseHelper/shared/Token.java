package de.ws1718.ismla.JapaneseHelper.shared;

public class Token {

	private String form;
	private String pronunciation;
	private String pos;
	// TODO make this a List<String> and split by the enumerators?
	private String translation;

	public Token(String form, String pronunciation, String pos, String translation) {
		this.form = form;
		this.pronunciation = pronunciation;
		this.pos = pos;
		this.translation = translation;
	}

	/**
	 * @return the form
	 */
	public String getForm() {
		return form;
	}

	/**
	 * @param form
	 *            the form to set
	 */
	public void setForm(String form) {
		this.form = form;
	}

	/**
	 * @return the pronunciation
	 */
	public String getPronunciation() {
		return pronunciation;
	}

	/**
	 * @param pronunciation
	 *            the pronunciation to set
	 */
	public void setPronunciation(String pronunciation) {
		this.pronunciation = pronunciation;
	}

	/**
	 * @return the POS tag
	 */
	public String getPos() {
		return pos;
	}

	/**
	 * @param pos
	 *            the POS tag to set
	 */
	public void setPos(String pos) {
		this.pos = pos;
	}

	/**
	 * @return the translation
	 */
	public String getTranslation() {
		return translation;
	}

	/**
	 * @param translation
	 *            the translation to set
	 */
	public void setTranslation(String translation) {
		this.translation = translation;
	}

	@Override
	public String toString() {
		return "Token [form=" + form + ", pronunciation=" + pronunciation + ", pos=" + pos + ", translation="
				+ translation + "]";
	}

	public void merge(Token other) {
		// TODO
	}

}

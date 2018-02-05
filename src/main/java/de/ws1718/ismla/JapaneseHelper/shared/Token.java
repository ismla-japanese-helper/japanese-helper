package de.ws1718.ismla.JapaneseHelper.shared;

import java.io.Serializable;

public class Token implements Serializable {

	private static final long serialVersionUID = -150279039676314554L;

	private String form;
	private String pronunciation;
	private String pos;
	private String inflectionParadigm;
	// TODO make this a List<String> and split by the enumerators?
	private String translation;
	private String inflectionInformation;

	public Token() {
		// Not sure if I should fill them this way. Let's just try it then.
		this("dummy", "dummy", "dummy", "dummy");
	}

	public Token(String form, String pronunciation, String pos, String translation) {
		this(form, pronunciation, pos, translation, null);
	}

	public Token(String form, String pronunciation, String pos, String translation, String inflectionInformation) {
		this.form = form;
		this.pronunciation = pronunciation;
		if (pos == null || pos.trim().isEmpty()) {
			this.pos = "";
		}
		if (pos.contains("[") && pos.contains("]")) {
			int open = pos.indexOf("[");
			int close = pos.indexOf("]");
			if (open < close) {
				this.pos = pos.substring(0, open);
				inflectionParadigm = pos.substring(open + 1, close);
				return;
			}
		} else {
			this.pos = pos;
		}
		this.translation = translation;
		this.inflectionInformation = inflectionInformation;
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

	/**
	 * @return the inflectionInformation
	 */
	public String getInflectionInformation() {
		return inflectionInformation;
	}

	/**
	 * @param inflectionInformation
	 *            the inflectionInformation to set
	 */
	public void setInflectionInformation(String inflection) {
		this.inflectionInformation = inflection;
	}

	public String getInflectionParadigm() {
		return inflectionParadigm;
	}

	public boolean isPredicate() {
		return inflectionParadigm != null;
	}

	@Override
	public String toString() {
		return form + "\t" + pronunciation + "\t" + pos + "\t" + translation
				+ (inflectionInformation == null || inflectionInformation.isEmpty() ? ""
						: "\t" + inflectionInformation);
	}

	public void merge(Token other) {
		// TODO
	}

}

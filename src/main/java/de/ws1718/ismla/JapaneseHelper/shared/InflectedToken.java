package de.ws1718.ismla.JapaneseHelper.shared;

public class InflectedToken extends Token {

	private static final long serialVersionUID = -8227675932132742011L;
	private String lemma;
	private String inflection;

	public InflectedToken(Token lemmaToken, String form, String pronunciation, String inflection) {
		this(form, pronunciation, lemmaToken.getPos(), lemmaToken.getTranslation(), lemmaToken.getForm(), inflection);
	}

	public InflectedToken(String form, String pronunciation, String pos, String translation, String lemma,
			String inflection) {
		super(form, pronunciation, pos, translation);
		this.lemma = lemma;
		this.inflection = inflection;
	}

	/**
	 * @return the lemma
	 */
	public String getLemma() {
		return lemma;
	}

	/**
	 * @param lemma
	 *            the lemma to set
	 */
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	/**
	 * @return the inflection
	 */
	public String getInflection() {
		return inflection;
	}

	/**
	 * @param inflection
	 *            the inflection to set
	 */
	public void setInflection(String inflection) {
		this.inflection = inflection;
	}

	@Override
	public String toString() {
		return super.toString() + "\t" + inflection + "\t" + lemma;
	}

}

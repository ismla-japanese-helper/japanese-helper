package de.ws1718.ismla.JapaneseHelper.shared;

public class InflectedToken extends Token {

	private static final long serialVersionUID = -8227675932132742011L;
	private String lemma;
	private String inflection;

	public InflectedToken() {
		// TODO Not sure if I should fill them this way. Let's just try it then.
		this(new Token("dummy", "dummy", "dummy", "1) dummy"), "dummy", "dummy", "dummy");
	}

	public InflectedToken(Token lemmaToken, String form, String pronunciation, String inflection) {
		this(form, pronunciation, lemmaToken.getPos(), lemmaToken.getTranslation(), inflection, lemmaToken.getForm());
	}

	public InflectedToken(String form, String pronunciation, String pos, String translation, String inflection,
			String lemma) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inflection == null) ? 0 : inflection.hashCode());
		result = prime * result + ((lemma == null) ? 0 : lemma.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof InflectedToken)) {
			return false;
		}
		InflectedToken other = (InflectedToken) obj;
		if (inflection == null) {
			if (other.inflection != null) {
				return false;
			}
		} else if (!inflection.equals(other.inflection)) {
			return false;
		}
		if (lemma == null) {
			if (other.lemma != null) {
				return false;
			}
		} else if (!lemma.equals(other.lemma)) {
			return false;
		}
		return true;
	}
	
	

}

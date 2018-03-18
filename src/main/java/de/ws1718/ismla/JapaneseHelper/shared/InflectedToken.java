package de.ws1718.ismla.JapaneseHelper.shared;

public class InflectedToken extends Token {

	private static final long serialVersionUID = -8227675932132742011L;
	private InflectableToken lemmaToken;
	private Inflection inflection;

	public InflectedToken() {
		this(new InflectableToken(), "", "", null);
	}

	/**
	 * Constructs a new inflected token.
	 * 
	 * @param lemmaToken
	 *            the lemmaToken of this token
	 * @param form
	 *            the form (in kanji and kana)
	 * @param pronunciation
	 *            the pronunciation (in kana)
	 * @param inflection
	 *            the aspect/tense/voice/politeness/negation according to which
	 *            this token is inflected
	 */
	public InflectedToken(InflectableToken lemmaToken, String form, String pronunciation, Inflection inflection) {
		super(form, pronunciation, lemmaToken.getPos(), lemmaToken.getInflectionParadigm(),
				lemmaToken.getTranslations());
		this.lemmaToken = lemmaToken;
		this.inflection = inflection;
	}

	/**
	 * @return the lemma token
	 */
	public InflectableToken getLemmaToken() {
		return lemmaToken;
	}

	/**
	 * @param lemmaToken
	 *            the lemma token to set
	 */
	public void setLemmaToken(InflectableToken lemmaToken) {
		this.lemmaToken = lemmaToken;
	}

	/**
	 * @return the inflection
	 */
	public Inflection getInflection() {
		return inflection;
	}

	/**
	 * @param inflection
	 *            the inflection to set
	 */
	public void setInflection(Inflection inflection) {
		this.inflection = inflection;
	}

	public String getLemmaAndInflectionInformation() {
		return lemmaToken.getForm() + " (" + getInflectionInformation() + ")";
	}
	
	@SuppressWarnings("incomplete-switch")
	public String getInflectionInformation() {
		String infl = inflection.toString();

		if ("ichi".equals(getInflectionParadigm()) || "dekiru".equals(getInflectionParadigm())
				|| "suru".equals(getInflectionParadigm()) || "suru-indep".equals(getInflectionParadigm())) {
			switch (inflection) {
			case IMPERATIVE:
				infl = "written " + infl;
				break;
			case IMPERATIVE2:
				infl = "spoken " + infl;
				break;
			case NEGATIVE3:
				if ("dekiru".equals(getInflectionParadigm())) {
					infl = "colloquial " + infl;
				}
				break;
			case POTENTIAL2:
				// can only be the case if the inflection paradim is "ichi"
				infl = "colloquial " + infl;
				break;
			}
		}
		
		return infl;
	}

	@Override
	public String toString() {
		return super.toString() + "\t" + getInflectionInformation() + "\t" + lemmaToken.getForm();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inflection == null) ? 0 : inflection.hashCode());
		result = prime * result + ((lemmaToken == null) ? 0 : lemmaToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || !(obj instanceof InflectedToken)) {
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
		if (lemmaToken == null) {
			return other.lemmaToken == null;
		}
		return lemmaToken.equals(other.lemmaToken);
	}

}

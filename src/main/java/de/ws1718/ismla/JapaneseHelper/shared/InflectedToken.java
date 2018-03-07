package de.ws1718.ismla.JapaneseHelper.shared;

import java.util.ArrayList;
import java.util.List;

public class InflectedToken extends Token {

	private static final long serialVersionUID = -8227675932132742011L;
	private String lemma;
	private String inflection;

	public InflectedToken() {
		this("", "", "", "", new ArrayList<String>(), "", "");
	}

	/**
	 * Constructs a new inflected token.
	 * 
	 * @param lemmaToken
	 *            the lemma of this token
	 * @param form
	 *            the form (in kanji and kana)
	 * @param pronunciation
	 *            the pronunciation (in kana)
	 * @param inflection
	 *            the aspect/tense/voice/politeness/negation according to which
	 *            this token is inflected
	 */
	public InflectedToken(Token lemmaToken, String form, String pronunciation, String inflection) {
		this(form, pronunciation, lemmaToken.getPos(), lemmaToken.getInflectionParadigm(), lemmaToken.getTranslations(),
				inflection, lemmaToken.getForm());
	}

	/**
	 * Constructs a new inflected token.
	 * 
	 * @param form
	 *            the form (in kanji and kana)
	 * @param pronunciation
	 *            the pronunciation (in kana)
	 * @param posSimple
	 *            the POS tag
	 * @param inflectionParadigm
	 *            the inflection paradigm
	 * @param translations
	 *            the list of translations
	 * @param inflection
	 *            the aspect/tense/voice/politeness/negation according to which
	 *            this token is inflected
	 * @param lemma
	 *            the lemma of this token
	 */
	public InflectedToken(String form, String pronunciation, String posSimple, String inflectionParadigm,
			List<String> translations, String inflection, String lemma) {
		super(form, pronunciation, posSimple, inflectionParadigm, translations);
		this.lemma = lemma;
		this.inflection = inflection;
	}

	/**
	 * Constructs a new inflected token.
	 * 
	 * @param form
	 *            the form (in kanji and kana)
	 * @param pronunciation
	 *            the pronunciation (in kana)
	 * @param posSimple
	 *            the POS tag
	 * @param inflectionParadigm
	 *            the inflection paradigm
	 * @param translation
	 *            the enumeration of translations, e.g.
	 *            "1) the first meaning 2) the second meaning 3) etc."
	 * @param inflection
	 *            the aspect/tense/voice/politeness/negation according to which
	 *            this token is inflected
	 * @param lemma
	 *            the lemma of this token
	 */
	public InflectedToken(String form, String pronunciation, String posSimple, String inflectionParadigm,
			String translation, String inflection, String lemma) {
		super(form, pronunciation, new String[] { posSimple, inflectionParadigm }, translation);
		this.lemma = lemma;
		this.inflection = inflection;
	}

	/**
	 * Constructs a new inflected token.
	 * 
	 * @param form
	 *            the form (in kanji and kana)
	 * @param pronunciation
	 *            the pronunciation (in kana)
	 * @param posAndInflection
	 *            a String array of length 2 containing the POS tag and the
	 *            inflection paradigm, in this order
	 * @param translation
	 *            the enumeration of translations, e.g.
	 *            "1) the first meaning 2) the second meaning 3) etc."
	 * @param inflection
	 *            the aspect/tense/voice/politeness/negation according to which
	 *            this token is inflected
	 * @param lemma
	 *            the lemma of this token
	 */
	public InflectedToken(String form, String pronunciation, String posAndInflection, String translation,
			String inflection, String lemma) {
		super(form, pronunciation, posAndInflection, translation);
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
		if (lemma == null) {
			return other.lemma == null;
		}
		return lemma.equals(other.lemma);
	}

}

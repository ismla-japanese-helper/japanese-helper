package de.ws1718.ismla.JapaneseHelper.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Token implements Serializable {

	private static final long serialVersionUID = -150279039676314554L;

	// TODO check if we need to change the lists to the serializable type
	// ArrayList
	private String form;
	private String pronunciation;
	private String pos;
	private String inflectionParadigm;
	private List<String> translations;

	/**
	 * Default constructor.
	 */
	public Token() {
		this("", "", "", null, new ArrayList<String>());
	}

	/**
	 * Constructs a new token.
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
	public Token(String form, String pronunciation, String posAndInflection, String translation) {
		this(form, pronunciation, processPosAndInflection(posAndInflection), translation);
	}

	/**
	 * Constructs a new token.
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
	 */
	public Token(String form, String pronunciation, String[] posAndInflection, String translation) {
		this(form, pronunciation, posAndInflection[0], posAndInflection[1], processGlosses(translation));
	}

	/**
	 * Constructs a new token.
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
	 *            the list of translations
	 */
	public Token(String form, String pronunciation, String posSimple, String inflectionParadigm,
			List<String> translations) {
		this.form = form;
		this.pronunciation = pronunciation;
		pos = posSimple;
		this.inflectionParadigm = inflectionParadigm;
		this.translations = translations;
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
	 * @return the pos
	 */
	public String getPos() {
		return pos;
	}

	/**
	 * @param pos
	 *            the pos to set
	 */
	public void setPos(String pos) {
		this.pos = pos;
	}

	/**
	 * @return the inflection paradigm
	 */
	public String getInflectionParadigm() {
		return inflectionParadigm;
	}

	/**
	 * @param inflectionParadigm
	 *            the inflection paradigm to set
	 */
	public void setInflectionParadigm(String inflectionParadigm) {
		this.inflectionParadigm = inflectionParadigm;
	}

	/**
	 * @return the translations
	 */
	public List<String> getTranslations() {
		return translations;
	}

	/**
	 * @param translations
	 *            the list of translations to set
	 */
	public void setTranslations(List<String> translations) {
		this.translations = translations;
	}

	/**
	 * @return true if this token is associated with an inflection paradigm
	 */
	public boolean inflects() {
		return inflectionParadigm != null || !inflectionParadigm.isEmpty();
	}

	@Override
	public String toString() {
		return form + "\t" + pronunciation + "\t" + pos + (inflects() ? "[" + inflectionParadigm + "]" : "") + "\t"
				+ translations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((form == null) ? 0 : form.hashCode());
		result = prime * result + ((translations == null) ? 0 : translations.hashCode());
		result = prime * result + ((inflectionParadigm == null) ? 0 : inflectionParadigm.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + ((pronunciation == null) ? 0 : pronunciation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof Token)) {
			return false;
		}
		Token other = (Token) obj;
		if (form == null) {
			if (other.form != null) {
				return false;
			}
		} else if (!form.equals(other.form)) {
			return false;
		}
		if (translations == null) {
			if (other.translations != null) {
				return false;
			}
		} else if (!translations.equals(other.translations)) {
			return false;
		}
		if (inflectionParadigm == null) {
			if (other.inflectionParadigm != null) {
				return false;
			}
		} else if (!inflectionParadigm.equals(other.inflectionParadigm)) {
			return false;
		}
		if (pos == null) {
			if (other.pos != null) {
				return false;
			}
		} else if (!pos.equals(other.pos)) {
			return false;
		}
		if (pronunciation == null) {
			return other.pronunciation == null;
		}
		return pronunciation.equals(other.pronunciation);
	}

	private static String[] processPosAndInflection(String pos) {
		if (pos == null || pos.trim().isEmpty()) {
			return new String[] { "", null };
		}

		String inflectionParadigm = null;
		int open = pos.indexOf("[");
		int close = pos.indexOf("]");
		if (open != -1 && close != -1 && open < close) {
			inflectionParadigm = pos.substring(open + 1, close);
			pos = pos.substring(0, open);
		}

		return new String[] { pos, inflectionParadigm };
	}

	/**
	 * Only for use in tester classes.
	 * 
	 * @param glosses
	 * @return
	 */
	public ArrayList<String> processGlossesTester(String glosses) {
		return processGlosses(glosses);
	}

	private static ArrayList<String> processGlosses(String glosses) {
		ArrayList<String> results = new ArrayList<>();
		// The position of the last index digit, e.g. '1' in "1) "
		int lastIndexPos = 0;
		String lastGloss = "";

		for (int curPointer = 0; curPointer < glosses.length(); curPointer++) {
			if (curPointer + 1 != glosses.length()) {
				// Match an index term, e.g. 1)
				if (Character.isDigit(glosses.charAt(curPointer)) && glosses.charAt(curPointer + 1) == ')') {
					// If it's not the first gloss, we should have already
					// recorded something previously
					if (glosses.charAt(curPointer) != '1') {
						// Maybe I should keep the index after all (instead of
						// +3). Let's see how this works.
						lastGloss = glosses.substring(lastIndexPos, curPointer - 1);
					}
					// Record the index position. This also works if it's the
					// first gloss.
					lastIndexPos = curPointer;
				}
			} else { // We've come to the end of the whole translations string
						// and we
						// should record the last entry anyways.
				// There is no extra space to deal with now. Need to stretch it
				// to the end.
				lastGloss = glosses.substring(lastIndexPos, curPointer + 1);
			}

			// If we already got a gloss at the end of this step, we add it.
			if (!lastGloss.isEmpty()) {
				// A lot of entries are "?". We don't want to add such entries.
				if (!lastGloss.equals("?")) {
					results.add(lastGloss);
				}
				lastGloss = "";
			}
		}
		return results;
	}

}

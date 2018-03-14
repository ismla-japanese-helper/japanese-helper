package de.ws1718.ismla.JapaneseHelper.shared;

import java.io.Serializable;
import java.util.ArrayList;

public class Token implements Serializable {

	private static final long serialVersionUID = -150279039676314554L;

	private String form;
	private String pronunciation;
	private String pos;
	private String prettyPos;
	private String inflectionParadigm;
	// `Array`List because it needs to be serializable
	private ArrayList<String> translations;

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
	 * @param translations
	 *            the list of translations
	 */
	public Token(String form, String pronunciation, String posSimple, String inflectionParadigm,
			ArrayList<String> translations) {
		this.form = form;
		this.pronunciation = pronunciation;
		pos = posSimple;
		prettyPos = cleanPosTag(posSimple);
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
	 * @return the long version of the POS tag
	 */
	public String getPrettyPos() {
		return prettyPos;
	}

	/**
	 * @param prettyPos
	 *            the long version of the POS tag
	 */
	public void setPrettyPos(String prettyPos) {
		this.prettyPos = prettyPos;
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
	public ArrayList<String> getTranslations() {
		return translations;
	}

	/**
	 * @param translations
	 *            the list of translations to set
	 */
	public void setTranslations(ArrayList<String> translations) {
		this.translations = translations;
	}

	// public void addTranslation(String translation) {
	// 	this.translations.add(translation);
	// }

	/**
	 * @return true if this token is associated with an inflection paradigm
	 */
	public boolean inflects() {
		return !(inflectionParadigm == null || inflectionParadigm.isEmpty());
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

	private static String cleanPosTag(String pos) {
		if (pos.startsWith("V") && pos.length() > 1) {
			return cleanVerbPosTag(pos);
		}

		switch (pos) {
		// sorted by frequency
		case "V":
			pos = "verb";
		case "A":
			pos = "adjective";
			break;
		case "N":
			pos = "noun";
			break;
		case "NE":
			pos = "named entity";
			break;
		case "ADV":
			pos = "adverb";
			break;
		case "SFX":
			pos = "suffix";
			break;
		case "ITJ":
			pos = "interjection";
			break;
		case "PRN":
			pos = "pronoun";
			break;
		case "CNT":
			pos = "counter";
			break;
		case "PFX":
			pos = "prefix";
			break;
		case "PRT":
			pos = "particle";
			break;
		case "CNJ":
			pos = "conjunction";
			break;
		case "NUM":
			pos = "numeral";
			break;
		case "DET":
			pos = "adnominal";
			break;
		case "PNC":
			pos = "punctuation";
			break;
		case "PSP":
			pos = "postposition";
			break;
		}

		return pos;
	}

	private static String cleanVerbPosTag(String pos) {
		String verbType = "";
		if (pos.contains("1") || pos.contains("5") || pos.contains("godan")) {
			// Most entries for godan verbs contain "1",
			// but there are a few irregularities.
			verbType = "godan ";
		} else if (pos.contains("2")) {
			verbType = "ichidan ";
		} else if (pos.contains("3")) {
			verbType = "irregular ";
		} else if (pos.contains("yo")) {
			verbType = "yodan ";
		} else if (pos.contains("ni")) {
			verbType = "nidan ";
		}

		String transitivity = "";
		if (pos.contains("I")) {
			transitivity = "intransitive ";
		} else if (pos.contains("T")) {
			transitivity = "transitive ";
		} else if (pos.contains("B")) {
			transitivity = "bitransitive ";
		}

		String inflectedForm = "";
		if (pos.contains("form")) {
			inflectedForm = " (infl.)";
		}

		return transitivity + verbType + "verb" + inflectedForm;
	}

	public static ArrayList<String> processGlosses(String glosses) {
		glosses = cleanTranslation(glosses);

		ArrayList<String> results = new ArrayList<>();
		// The position of the last index digit, e.g. '1' in "1) "
		int lastIndexPos = 0;
		String lastGloss = "";

		for (int curPointer = 0; curPointer < glosses.length(); curPointer++) {
			if (curPointer + 1 != glosses.length()) {
				// Match an index term, e.g. "1)"
				if (Character.isDigit(glosses.charAt(curPointer)) && glosses.charAt(curPointer + 1) == ')') {
					// If it's not the first gloss, we should have already
					// recorded something previously
					if (glosses.charAt(curPointer) != '1' && curPointer > lastIndexPos + 3) {
						// Remove the index term from the translation entry.
						lastGloss = glosses.substring(lastIndexPos + 3, curPointer - 1);
					}
					// Record the index position. This also works if it's the
					// first gloss.
					lastIndexPos = curPointer;
				}
			} else if (curPointer >= lastIndexPos + 3) { // There are some totally empty entries. Need to check against that.
				// We've come to the end of the whole translations string
				// and we should record the last entry anyways.
				// There is no extra space to deal with now. Need to stretch it
				// to the end.
				lastGloss = glosses.substring(lastIndexPos + 3, curPointer + 1);
			}

			// If we already got a gloss at the end of this step, we add it.
			if (!lastGloss.isEmpty()) {
				lastGloss = lastGloss.trim();
				// A lot of entries are "?". We don't want to add such entries.
				// Don't forget to take the substring now that we've also included the index number actually.
				if (!lastGloss.equals("?")) {
					results.add(lastGloss);
				}
				lastGloss = "";
			}
		}

		// Add a check on whether the first meaning is archaic. If yes, move it to the end.
		// Sometimes we have absolutely empty entries for some reason... So we'll have to check that first.
		if (results.size() > 1) {
			String firstEntry = results.get(0);
			// if (firstEntry.length() > 9 && firstEntry.contains("[archaic]")) {
			if (firstEntry.contains("[archaic]")) {
				results.remove(0);
				results.add(firstEntry);
			}
		}

		// Sometimes the whole thing is empty... In this case we'll have to return something anyways.
		if (results.size() == 0) {
			results.add("[out-of-vocabulary]");
		}

		return results;
	}

	private static String cleanTranslation(String translation) {
		translation = translation.trim();

		// The escaped HTML sometimes include extra spaces
		// or lack the final semicolon.
		translation = translation.replaceAll("&amp;?", "&");
		translation = translation.replaceAll("& ?lt;?", "<");
		translation = translation.replaceAll("& ?gt;?", ">");
		translation = translation.replaceAll("& ?quot;? ?", "\"");
		translation = translation.replaceAll("& ?ndash;? ?", "–");
		translation = translation.replaceAll("& ?mdash;? ?", "—");
		translation = translation.replaceAll("& ?nbsp;? ?", "u\00A0");
		translation = translation.replaceAll("& ?hellip;? ?", "u\2026");
		translation = translation.replaceAll("& ?times;? ?", "×");
		translation = translation.replaceAll("''", "\"");

		// Remove HTML comments.
		int commentStartIndex = translation.indexOf("< !--");
		int commentEndIndex = translation.indexOf("-->");
		while (commentStartIndex > -1) {
			if (commentEndIndex == -1) {
				translation = translation.substring(0, commentStartIndex);
			} else {
				translation = translation.substring(0, commentStartIndex) + translation.substring(commentEndIndex + 3);
			}
			commentStartIndex = translation.indexOf("< !--");
			commentEndIndex = translation.indexOf("-->");
		}

		// Enumerations use sequences of hash signs in the Wiktionary dump.
		translation = translation.replaceAll("#{2,} ", "•");
		translation = translation.replaceAll("#+$", "");

		// {{, }}
		translation = translation.replaceAll("(\\{\\{)|(\\}\\})", "");

		translation = translation.replaceAll("\\s+", " ");
		return translation;
	}

	/**
	 * Merges another token into this one. Assumes that both tokens share the
	 * same form.
	 * 
	 * @param other
	 *            the other token
	 */
	public void merge(Token other) {
		pronunciation = merge(pronunciation, other.pronunciation);
		pos = merge(pos, other.pos);
		inflectionParadigm = merge(inflectionParadigm, other.inflectionParadigm);
		translations.addAll(other.translations);
	}

	private String merge(String field, String other) {
		field = field == null ? "" : field;
		other = other == null ? "" : other;
		return field + ", " + other;
	}

}

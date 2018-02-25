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

	public Token() {
		// TODO Not sure if I should fill them this way. Let's just try it then.
		this("dummy", "dummy", "dummy", "dummy");
	}

	public Token(String form, String pronunciation, String pos, String translation) {
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
			}
		} else {
			this.pos = pos;
		}
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

	public String getInflectionParadigm() {
		return inflectionParadigm;
	}

	public boolean isPredicate() {
		return inflectionParadigm != null;
	}

	@Override
	public String toString() {
		return form + "\t" + pronunciation + "\t" + pos + (isPredicate() ? "[" + inflectionParadigm + "]" : "") + "\t"
				+ translation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((form == null) ? 0 : form.hashCode());
		result = prime * result + ((inflectionParadigm == null) ? 0 : inflectionParadigm.hashCode());
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		result = prime * result + ((pronunciation == null) ? 0 : pronunciation.hashCode());
		result = prime * result + ((translation == null) ? 0 : translation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Token)) {
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
			if (other.pronunciation != null) {
				return false;
			}
		} else if (!pronunciation.equals(other.pronunciation)) {
			return false;
		}
		if (translation == null) {
			if (other.translation != null) {
				return false;
			}
		} else if (!translation.equals(other.translation)) {
			return false;
		}
		return true;
	}

	public void merge(Token other) {
		// TODO
	}

}

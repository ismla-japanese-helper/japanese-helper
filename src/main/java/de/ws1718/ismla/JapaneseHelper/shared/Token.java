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

}

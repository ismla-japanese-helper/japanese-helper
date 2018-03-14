package de.ws1718.ismla.JapaneseHelper.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Test;

import de.ws1718.ismla.JapaneseHelper.shared.Token;

public class GlossesTest {

	@Test
	public void testProcessGlosses() {
		String s = "1) a land, a large place 2) a country in general, a region 3) a country as in a nation, a state "
				+ "4) the office of emperor, the crown; affair s of state 5) a province of ancient Japan "
				+ "6) the national  government in ancient Japan; the national capital in ancient Japan "
				+ "7) one's birthplace";
		List<String> expected = Arrays.asList("a land, a large place", "a country in general, a region",
				"a country as in a nation, a state", "the office of emperor, the crown; affair s of state",
				"a province of ancient Japan",
				"the national government in ancient Japan; " + "the national capital in ancient Japan",
				"one's birthplace");
		ArrayList<String> results = Token.processGlosses(s);
		assertEquals(expected, results);

		s = "1) ? 2) to sleep 3) to leave";
		results = Token.processGlosses(s);
		expected = Arrays.asList("to sleep", "to leave");
		assertEquals(expected, results);

		s = "1) ? 2) ? 3) ? 4) loss 5) barrel";
		results = Token.processGlosses(s);
		expected = Arrays.asList("loss", "barrel");
		assertEquals(expected, results);
	}

	@Test
	public void testCleanTranslation() {
		String s = "1) surnameThe 5th most common surname in Japan.&lt; ref&gt; 姓#日本の主な名字 - "
				+ "Japanese Wikipedia&lt; ref&gt";
		List<String> expected = Arrays
				.asList("surnameThe 5th most common surname in Japan.< ref> 姓#日本の主な名字 - Japanese Wikipedia< ref>");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) white dew&lt; !--草木に置いて、白く光って見える露。--&gt";
		expected = Arrays.asList("white dew");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) eighty-eight years old [colloquial] "
				+ "2) rice&lt; !-- probably belongs in a separate section --&gt [archaic]";
		expected = Arrays.asList("eighty-eight years old [colloquial]", "rice [archaic]");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) metre, meter  &lt; !-- Which word does this belong with?";
		expected = Arrays.asList("metre, meter");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) trevally, striped jack&lt; !-- are these the same? my local shushi shop called it "
				+ "trevally with the kanaromaji here. on the web i find striped jack, possibly "
				+ "different local names in different countries --&gt; (a kind of fish used in sushi)";
		expected = Arrays.asList("trevally, striped jack (a kind of fish used in sushi)");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) text1 &lt; !-- comment1 --&gt;  text2 &lt; !-- comment2 --&gt; text3 &lt; !-- comment3";
		expected = Arrays.asList("text1 text2 text3");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) must, shall ###";
		expected = Arrays.asList("must, shall");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) variant of 汀,: the water's edge: ## beach ## shore ## bank";
		expected = Arrays.asList("variant of 汀,: the water's edge: •beach •shore •bank");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) to make thin sheets of something by spreading out pulp and drying: ## to make paper #";
		expected = Arrays.asList("to make thin sheets of something by spreading out pulp and drying: •to make paper");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) [element in compounds, referring to things {{l]";
		expected = Arrays.asList("[element in compounds, referring to things l]");
		assertEquals(expected, Token.processGlosses(s));

		s = "1) coarse, rough, plain, poor, shabby 2) to neglect [{{lang, ja, 粗末にする}}]";
		expected = Arrays.asList("coarse, rough, plain, poor, shabby", "to neglect [lang, ja, 粗末にする]");
		assertEquals(expected, Token.processGlosses(s));

	}

}

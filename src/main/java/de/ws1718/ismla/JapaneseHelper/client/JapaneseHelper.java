package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.ArrayList;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class JapaneseHelper implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network " + "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);

	public void onModuleLoad() {
		List<Token> testList = new ArrayList<>();
		Token t1 = new Token("皆さん", "みなさん", "N", "We");
		Token t2 = new Token("皆さん", "みなさん", "N", "We");
		testList.add(t1);
		testList.add(t2);

		ResultsWidget rw = new ResultsWidget(testList);

		RootPanel.get().add(rw);

		greetingService.greetServer("", new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
			}
			public void onSuccess(String result) {
			}
		});

	}
}

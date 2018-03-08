package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class JapaneseHelper implements EntryPoint {

	public void onModuleLoad() {
		RootPanel.get("inputContainer").add(new SentenceInputWidget());
	}
}

package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.List;

// I'm not sure how to use the Async service yet. Let's leave it for later then.
public interface LookupServiceAsync {
    void lookup(String sentence, AsyncCallback<List<Token>> callback);
}

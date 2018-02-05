package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.List;

public interface LookupServiceAsync {
    void lookup(String sentence, AsyncCallback<List<Token>> callback);

    public static final class Util {
        private static LookupServiceAsync instance;

        public static final LookupServiceAsync getInstance() {
            if (instance == null) {
                instance = (LookupServiceAsync) GWT.create(LookupService.class);
            }
            return instance;
        }

        private Util() {
            // Utility class should not be instantiated
        }
    }
}

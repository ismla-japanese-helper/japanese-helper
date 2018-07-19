package de.ws1718.ismla.JapaneseHelper.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.gwt.core.client.GWT;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import java.util.ArrayList;
import java.util.List;

@RemoteServiceRelativePath("LookupService")
public interface LookupService extends RemoteService {
    /**
     * Utility/Convenience class.
     * Use LookupService.App.getInstance() to access static instance of LookupServiceAsync
     */
    List<ArrayList<Token>> lookup(String sentence);
    
    String tokenizeFiles();

    public static class App {
        private static final LookupServiceAsync ourInstance = (LookupServiceAsync) GWT.create(LookupService.class);

        public static LookupServiceAsync getInstance() {
            return ourInstance;
        }
    }
}

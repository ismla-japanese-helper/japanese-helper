package de.ws1718.ismla.JapaneseHelper.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import de.ws1718.ismla.JapaneseHelper.client.LookupService;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

import com.atilika.kuromoji.ipadic.Tokenizer;

import java.util.ArrayList;
import java.util.List;

public class LookupServiceImpl extends RemoteServiceServlet implements LookupService {
    public List<Token> lookup(String sentence) {
        Tokenizer tokenizer = new Tokenizer();
        // This is the Token defined by the Kuromoji parser.
        List<com.atilika.kuromoji.ipadic.Token> tokens = tokenizer.tokenize(sentence);

        // This is the Token defined by us.
        List<Token> results = new ArrayList<>();
        // Deal with this part of the code later.
        for (com.atilika.kuromoji.ipadic.Token t : tokens) {
            Token convertedToken = convertToken(t);
            results.add(convertedToken);
        }

        // Dummy code. Waiting for the backend implementation to finish.
        return results;
    }

    private Token convertToken(com.atilika.kuromoji.ipadic.Token t) {
        // TODO: To be implemented
        // This is dummy code.
//        return new Token("皆さん", "みなさん", "N", "1) We 2) archaic 3) test");
        return new Token("皆さん", "みなさん", "N", "1) a land, a large place 2) a country in general, a region 3) a country as in a nation, a state 4) the office of emperor, the crown; affair s of state 5) a province of ancient Japan 6) the national  government in ancient Japan; the national capital in ancient Japan 7) one's birthplace");
    }
}

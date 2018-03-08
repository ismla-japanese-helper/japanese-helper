package de.ws1718.ismla.JapaneseHelper.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

import com.google.common.collect.Multimap;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.ws1718.ismla.JapaneseHelper.client.GreetingService;
import de.ws1718.ismla.JapaneseHelper.shared.Token;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class GreetingServiceImpl extends RemoteServiceServlet implements GreetingService {

	public String greetServer(String input) throws IllegalArgumentException {
		return "done";
	}

}

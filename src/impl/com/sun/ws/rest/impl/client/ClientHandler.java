package com.sun.ws.rest.impl.client;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface ClientHandler {
    ClientResponse handle(ClientRequest ro);
}

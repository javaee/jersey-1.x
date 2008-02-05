package com.sun.ws.rest.impl.client;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class ClientFilter implements ClientHandler {
    private ClientHandler next;
    
    public final void setNext(ClientHandler next) {
        this.next = next;
    }
    
    public final ClientHandler getNext() {
        return next;
    }
    
    public abstract ClientResponse handle(ClientRequest ro);
}

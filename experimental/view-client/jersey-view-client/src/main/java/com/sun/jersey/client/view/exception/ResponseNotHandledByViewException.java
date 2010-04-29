/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.client.view.exception;

import com.sun.jersey.api.client.ClientResponse;

/**
 *
 * @author algermissen1971
 */
public class ResponseNotHandledByViewException extends ClientRuntimeException {

	private final ClientResponse cr;


	public ResponseNotHandledByViewException(ClientResponse cr) {
		this.cr = cr;
	}

	public ClientResponse getClientResponse() {
		return this.cr;
	}

}

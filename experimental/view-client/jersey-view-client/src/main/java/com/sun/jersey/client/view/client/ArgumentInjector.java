/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.client.view.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResourceLinkHeaders;
import com.sun.jersey.client.view.exception.ClientRuntimeException;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import java.lang.annotation.Annotation;
import java.net.URI;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Context;

/**
 *
 * @author algermissen1971
 */
public class ArgumentInjector {

	public static Object[] makeArgs(Client client, AnnotatedMethod m, ClientResponse cr, URI uri) {

		// Code requires every parameter (only annotations[0] is looked at) ->
		// FIXME
		Class<?>[] parameterTypes = m.getParameterTypes();
		Annotation[][] parameterAnnotations = m.getParameterAnnotations();
		Object[] args = new Object[parameterTypes.length];
		boolean haveEntityParam = false;
		for (int i = 0; i < parameterTypes.length; i++) {
			Annotation[] annotations = parameterAnnotations[i];
			// non-annotated parameter is entity
			if (annotations.length == 0) {
				if (haveEntityParam) {
					throw new ClientRuntimeException(
						"two non-annotated params not allowed");
				}
				args[i] = cr.getEntity(parameterTypes[i]);
				haveEntityParam = true;
			} else if (annotations[0].annotationType() == HeaderParam.class) {
				if (parameterTypes[i] == String.class) {
					String value = cr.getHeaders().getFirst(
						((HeaderParam) annotations[0]).value());
					args[i] = value;
				} else {
					throw new ClientRuntimeException(
						"Cannot inject this header as this type.");
				}
			} else if (annotations[0].annotationType() == Context.class) {
				if (parameterTypes[i] == Client.class) {
					args[i] = client;
				} else if (parameterTypes[i] == ClientResponse.class) {
					args[i] = cr;
				} else if (parameterTypes[i] == URI.class) {
					if (uri != null) {
						args[i] = uri;
					} else {
						throw new ClientRuntimeException(
							"Request URI cannot be injected in this context.");

					}
				} else if (parameterTypes[i] == WebResource.class) {
					if (uri != null) {
						args[i] = client.resource(uri);
					} else {
						throw new ClientRuntimeException(
							"Request URI cannot be injected in this context.");

					}
				} else if (parameterTypes[i] == ViewResource.class) {
					if (uri != null) {
						args[i] = client.viewResource(uri);
					} else {
						throw new ClientRuntimeException(
							"Request URI cannot be injected in this context.");

					}
				} else if (parameterTypes[i] == WebResourceLinkHeaders.class) {
                                    args[i] = cr.getLinks();
				}
			} else {
				args[i] = null;
				throw new ClientRuntimeException("Annotation of " + i + "th parameter not supported here.");
			}

		}
		return args;
	}

}

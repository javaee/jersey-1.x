/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jersey.client.view.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ViewResource;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResourceLinkHeaders;
import com.sun.jersey.client.view.annotation.Required;
import com.sun.jersey.client.view.exception.ClientRuntimeException;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URI;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.Context;

/**
 *
 * @author algermissen@acm.org
 */
public class ArgumentInjector {

	// TODO note somewhere that combinations of injecting body and stream or response are at user's responsibilty
	public static Object[] makeArgs(Client client, AnnotatedMethod m,
			ClientResponse cr, URI uri) {

		Class<?>[] parameterTypes = m.getParameterTypes();
		Annotation[][] parameterAnnotations = m.getParameterAnnotations();
		Object[] args = new Object[parameterTypes.length];
		boolean haveEntityParam = false;
		boolean responseOrOutputStreamInjected = false;

		for (int i = 0; i < parameterTypes.length; i++) {
			Annotation[] annotations = parameterAnnotations[i];

			// non-annotated parameter is entity
			if (annotations.length == 0) {
				// only one non-annotated parameter allowed
				if (haveEntityParam) {
					throw new ClientRuntimeException(
							"two non-annotated params not allowed, parameter #" + i);
				}
				args[i] = cr.getEntity(parameterTypes[i]);
				haveEntityParam = true;

				// this is an annotated parameter
			} else {
				boolean isRequired = hasRequiredAnnotation(annotations);

				Annotation selectorAnnotation = ArgumentInjector.
						getSelectorAnnotation(
						annotations);
				if (selectorAnnotation == null) {
					throw new ClientRuntimeException(
							"missing/unrecognized selector annotation on parameter #" + i);
				}

				// this is annotated with @HeaderParam
				if (selectorAnnotation.annotationType() == HeaderParam.class) {
					String headerValue = cr.getHeaders().getFirst(
							((HeaderParam) selectorAnnotation).value());
					if (isRequired && headerValue == null) {
						throw new ClientRuntimeException(
								"required header " + ((HeaderParam) selectorAnnotation).
								value() + "not present.");
					}
					if (headerValue == null) {
						args[i] = null;
					} else if (parameterTypes[i] == String.class) {
						args[i] = headerValue;
					} else if (parameterTypes[i] == URI.class) {
						args[i] = URI.create(headerValue);
					} else if (parameterTypes[i] == ViewResource.class) {
						args[i] = client.viewResource(headerValue);
					} else if (parameterTypes[i] == WebResource.class) {
						args[i] = client.resource(headerValue);
					} else {
						throw new ClientRuntimeException(
								"Cannot inject this header as this type.");
					}

					// this is anotated with @Context
				} else if (selectorAnnotation.annotationType() == Context.class) {
					if (parameterTypes[i] == Client.class) {
						args[i] = client;
					} else if (parameterTypes[i] == ClientResponse.class) {
						args[i] = cr;
						responseOrOutputStreamInjected = true;
					} else if (parameterTypes[i] == InputStream.class) {
						args[i] = cr.getEntityInputStream();
						responseOrOutputStreamInjected = true;
					} else if (parameterTypes[i] == URI.class) {
						if (uri != null) {
							args[i] = uri;
						} else {
							throw new ClientRuntimeException(
									"Request URI cannot be injected in this context.");
						}
					} else if (parameterTypes[i] == ViewResource.class) {
						if (uri != null) {
							args[i] = client.viewResource(uri);
						} else {
							throw new ClientRuntimeException(
									"ViewResource based on request URI cannot be injected in this context.");
						}
					} else if (parameterTypes[i] == WebResourceLinkHeaders.class) {
						 args[i] = cr.getLinks();
					} else {
						throw new ClientRuntimeException(
								"Unrecognized parameter type " + parameterTypes[i].
								getName() + " for @Context annotation in parameter at index " + i);

					}

					// The selector annotation is not known
				} else {
					throw new ClientRuntimeException(
							"Unsupported selector annotation on parameter #" + i);
				}
			}
		}
		// TODO use responseOrOutputStreamInjected somehow
		// to control closing of response object.
		return args;
	}

	static boolean hasRequiredAnnotation(Annotation[] a) {
		for (int i = 0; i < a.length; i++) {
			if (a[i].annotationType() == Required.class) {
				return true;
			}
		}
		return false;
	}

	static Annotation getSelectorAnnotation(Annotation[] a) {
		for (int i = 0; i < a.length; i++) {
			if (a[i].annotationType() == Context.class) {
				return a[i];
			}
			if (a[i].annotationType() == HeaderParam.class) {
				return a[i];
			}
		}
		return null;

	}
}

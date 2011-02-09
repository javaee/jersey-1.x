/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.test.framework.impl.container.inmemory;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.TerminatingClientHandler;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.core.header.reader.HttpHeaderReader;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.COM
 */
public class TestResourceClientHandler extends TerminatingClientHandler {
    private final WebApplication w;

    private final URI baseUri;

    public TestResourceClientHandler(URI baseUri, WebApplication w) {
        this.baseUri = baseUri;
        this.w = w;
    }

    private static class TestContainerResponseWriter implements ContainerResponseWriter {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        public OutputStream writeStatusAndHeaders(long contentLength,
                ContainerResponse response) throws IOException {
            return baos;
        }

        public void finish() throws IOException {
        }
    }

    public ClientResponse handle(ClientRequest clientRequest) {
        byte[] requestEntity = writeRequestEntity(clientRequest);

        InBoundHeaders rh = getInBoundHeaders(clientRequest.getMetadata());

        final ContainerRequest cRequest = new ContainerRequest(
                w,
                clientRequest.getMethod(),
                baseUri,
                clientRequest.getURI(),
                rh,
                new ByteArrayInputStream(requestEntity)
                );

        // TODO this is a hack
        List<String> cookies = cRequest.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie != null)
                    cRequest.getCookies().putAll(
                            HttpHeaderReader.readCookies(cookie));
            }
            }

        final TestContainerResponseWriter writer = new TestContainerResponseWriter();
        final ContainerResponse cResponse = new ContainerResponse(
                w,
                cRequest,
                writer);

        try {
            w.handleRequest(cRequest, cResponse);
        } catch (IOException e) {
            throw new ContainerException(e);
        }

        byte[] responseEntity = writer.baos.toByteArray();
        ClientResponse clientResponse = new ClientResponse(
                cResponse.getStatus(),
                getInBoundHeaders(cResponse.getHttpHeaders()),
                new ByteArrayInputStream(responseEntity),
                getMessageBodyWorkers());

        clientResponse.getProperties().put("request.entity", requestEntity);
        clientResponse.getProperties().put("response.entity", responseEntity);
        return clientResponse;
    }

    private InBoundHeaders getInBoundHeaders(MultivaluedMap<String, Object> outBound) {
        InBoundHeaders inBound = new InBoundHeaders();

        for (Map.Entry<String, List<Object>> e : outBound.entrySet()) {
            for (Object v : e.getValue()) {
                inBound.add(e.getKey(), headerValueToString(v));
            }
        }

        return inBound;
    }

    private byte[] writeRequestEntity(ClientRequest ro) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeRequestEntity(ro, new RequestEntityWriterListener() {

                public void onRequestEntitySize(long size) throws IOException {
                }

                public OutputStream onGetOutputStream() throws IOException {
                    return baos;
                }
            });
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new ClientHandlerException(ex);
        }
    }

}

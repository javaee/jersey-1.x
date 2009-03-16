/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package com.sun.jersey.api.client.filter;

import com.sun.jersey.api.client.AbstractClientRequestAdapter;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientRequestAdapter;
import com.sun.jersey.api.client.ClientResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;



/**
 * ConnectionListener filter
 *
 * This filter does not modify input/output stream
 *
 *
 * @author pavel.bucek@sun.com
 */
public class ConnectionListenerFilter extends ClientFilter {

    private static final class Adapter extends AbstractClientRequestAdapter {

        private final ContainerListener listener;

        Adapter(ClientRequestAdapter cra, ContainerListener listener) {
            super(cra);
            this.listener = listener;
        }

        public OutputStream adapt(ClientRequest request, OutputStream out) throws IOException {
            return new ReportingOutputStream(getAdapter().adapt(request, out), listener);
        }
    }

    private final OnStartConnectionListener listenerFactory;

    /**
     * Creates ConnectionListenerFilter.
     *
     * @param listenerFactory {@link OnStartConnectionListener} instance
     *
     */
    public ConnectionListenerFilter(OnStartConnectionListener listenerFactory) {

        if (listenerFactory == null) {
            throw new IllegalArgumentException("ConnectionListenerFilter can't be initiated without OnStartConnectionListener");
        }

        this.listenerFactory = listenerFactory;
    }

    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {

        // try catch finally block - onFinish() can be called.

        ContainerListener listener = listenerFactory.onStart(new ClientRequestContainer(request));

        request.setAdapter(new Adapter(request.getAdapter(), listener));

        ClientResponse response = getNext().handle(request);

        if (response.hasEntity()) {

            InputStream entityInputStream = response.getEntityInputStream();

            listener.onReceiveStart(response.getLength());

            response.setEntityInputStream(new ReportingInputStream(entityInputStream, listener));

        } else {
            listener.onFinish();
        }

        return response;
    }
}

class ReportingOutputStream extends OutputStream {

    private final OutputStream outputStream;
    private final ContainerListener listener;
    private long totalBytes = 0;

    public ReportingOutputStream(OutputStream outputStream, ContainerListener listener) {
        this.outputStream = outputStream;
        this.listener = listener;
    }

    private void report(long bytes) {
        totalBytes += bytes;
        listener.onSent(bytes, totalBytes);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
        report(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
        report(len);
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        report(1);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }
}


class ReportingInputStream extends InputStream {

    private final InputStream inputStream;
    private final ContainerListener listener;
    private int markPosition = 0;
    private long totalBytes = 0;

    private boolean finished = false;

    public ReportingInputStream(InputStream inputStream, ContainerListener listener) {
        this.inputStream = inputStream;
        this.listener = listener;
    }

    private void report(long bytes) { // return int and call read(*) like: return report(read(...)); ?
        if(bytes == -1) {
            finished = true;
            listener.onFinish();
        } else {
            totalBytes += bytes;
            listener.onReceived(bytes, totalBytes);
        }
    }

    @Override
    public int read() throws IOException {
        int readBytes = inputStream.read();
        if(readBytes == -1) {
            report(-1);
        } else {
            report(1);
        }
        return readBytes;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int readBytes = inputStream.read(b);
        report(readBytes);
        return readBytes;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readBytes = inputStream.read(b, off, len);
        report(readBytes);
        return readBytes;
    }

    @Override
    public long skip(long n) throws IOException {
        report(n);
        return inputStream.skip(n);
    }

    @Override
    public void close() throws IOException {
        if(!finished) listener.onFinish();
        inputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        markPosition = readlimit;
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        totalBytes = markPosition;
        inputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }
}

/**
 * Read-only version of ClientRequest
 *
 */
class ClientRequestContainer extends ClientRequest {

    private ClientRequest request;

    /* package */ ClientRequestContainer(ClientRequest request) {
        this.request = request;
    }

    @Override
    public Map<String, Object> getProperties() {
        if (request.getProperties() != null) {
            return Collections.unmodifiableMap(request.getProperties());
        } else {
            return null;
        }
    }

    @Override
    public URI getURI() {
        return request.getURI();
    }

    @Override
    public void setURI(URI uri) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public void setMethod(String method) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Object getEntity() {
        return request.getEntity();
    }

    @Override
    public void setEntity(Object entity) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * changing anything in returned multivalued map has to be forbidden
     *
     */
    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        if (request.getMetadata() != null) {
            return request.getMetadata(); // :-/ TODO (Collections.unmodifiableMultivaluedMap)
        } else {
            return null;
        }
    }

    @Override
    public ClientRequestAdapter getAdapter() {
        return request.getAdapter();
    }

    @Override
    public void setAdapter(ClientRequestAdapter adapter) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ClientRequest clone() {
        throw new UnsupportedOperationException("Not supported.");
    }
}



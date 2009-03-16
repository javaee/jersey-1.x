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
package com.sun.jersey.impl.client.filter;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ContainerListener;
import com.sun.jersey.api.client.filter.OnStartConnectionListener;
import com.sun.jersey.impl.container.grizzly.AbstractGrizzlyServerTester;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 *
 * @author pavel.bucek@sun.com
 */
public class ConnectionListenerTest extends AbstractGrizzlyServerTester {

    @Path("/")
    public static class Resource {
        @GET
        public String get1() {
            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < 1024; i++) { // 10kB string
                sb.append("0123456789".toCharArray());
            }
            return sb.toString();
        }

        @POST
        public String post1(String content) { return ""; }
    }

    public ConnectionListenerTest(String testName) {
        super(testName);
    }

    class ListenerFactory implements OnStartConnectionListener {

        private Listener listener = null;

        public ListenerFactory(Listener l) {
            listener = l;
        }

        public ContainerListener onStart(ClientRequest cr) {
            return listener;
        }
    }

    class Listener extends ContainerListener {

        public long bytes;
        public long bytesSent;
        public long totalBytes;
        public boolean onSent = false;
        public boolean onReceived = false;
        public boolean onFinish = false;

        @Override
        public void onSent(long delta, long bytes) {
            this.onSent = true;
            this.bytesSent = bytes;
        }

        @Override
        public void onReceiveStart(long totalBytes) {
            this.totalBytes = totalBytes;
        }

        @Override
        public void onReceived(long delta, long bytes) {
            onReceived = true;
            this.bytes = bytes;
        }

        @Override
        public void onFinish() {
            this.onFinish = true;
        }
    }

    /**
     * ContainerListener test - GET
     */
    public void testGet1() {
        startServer(Resource.class);

        Listener l = new Listener();

        Client c = Client.create();
        c.addFilter(new com.sun.jersey.api.client.filter.ConnectionListenerFilter(new ListenerFactory(l)));
        WebResource r = c.resource(getUri().build());

        r.get(String.class);

        assertTrue(l.totalBytes == -1); // server does not provide content-length
        assertTrue(l.bytes == 10240);
        assertTrue(l.onSent == false); // request content was empty
        assertTrue(l.onReceived == true);
        assertTrue(l.onFinish == true);

    }

    /**
     * ContainerListener test - POST
     */
    public void testPost1() {
        startServer(Resource.class);

        Listener l = new Listener();

        Client c = Client.create();
        c.addFilter(new com.sun.jersey.api.client.filter.ConnectionListenerFilter(new ListenerFactory(l)));
        WebResource r = c.resource(getUri().build());

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < 1024; i++) { // 10kB string
            sb.append("0123456789".toCharArray());
        }

        r.post(String.class, sb.toString());

        assertTrue(l.totalBytes == -1); // server does not provide content-length
        assertTrue(l.bytesSent == 10240);
        assertTrue(l.onSent == true);
        assertTrue(l.onReceived == false);
        assertTrue(l.onFinish == true);
    }
}
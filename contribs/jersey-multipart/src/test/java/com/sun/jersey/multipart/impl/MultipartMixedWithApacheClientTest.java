/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.multipart.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.multipart.Boundary;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.MultiPartMediaTypes;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class MultipartMixedWithApacheClientTest extends AbstractGrizzlyServerTester {

    public MultipartMixedWithApacheClientTest(String testName) {
        super(testName);
    }
    Client client = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        client = ApacheHttpClient.create();
    }

    @Override
    public void tearDown() {
        super.tearDown();
        client = null;
    }

    @Path("/")
    public static class ProducesFormDataUsingMultiPart {

        @POST
        @Consumes("multipart/mixed")
        public void post(MultiPart mp) throws IOException {
            byte[] in = read(mp.getBodyParts().get(0).getEntityAs(InputStream.class));
            assertEquals(50, in.length);

            in = read(mp.getBodyParts().get(1).getEntityAs(InputStream.class));
            assertEquals(900 * 1024, in.length);
        }

        private byte[] read(InputStream in) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read = -1;
            while ((read = in.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }

            return baos.toByteArray();
        }
    }


    // Test a response of type "multipart/form-data".  The example comes from
    // Section 6 of RFC 1867.
    public void testProducesFormDataUsingMultiPart() {
        startServer(ProducesFormDataUsingMultiPart.class);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 900 * 1024; i++)
            baos.write(65);
        
        MultiPart multiPartInput = new MultiPart().
                bodyPart(new ByteArrayInputStream("01234567890123456789012345678901234567890123456789".getBytes()), MediaType.APPLICATION_OCTET_STREAM_TYPE).
                bodyPart(baos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        client.resource(getUri()).
                type("multipart/mixed").post(multiPartInput);
    }

    public void testChunkedEncodingUsingMultiPart() {
        startServer(ProducesFormDataUsingMultiPart.class);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < 900 * 1024; i++)
            baos.write(65);

        MultiPart multiPartInput = new MultiPart().
                bodyPart(new ByteArrayInputStream("01234567890123456789012345678901234567890123456789".getBytes()), MediaType.APPLICATION_OCTET_STREAM_TYPE).
                bodyPart(baos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM_TYPE);

        client.setChunkedEncodingSize(1024);
        client.resource(getUri()).
                type(MultiPartMediaTypes.createMixed()).
                post(multiPartInput);
    }

}

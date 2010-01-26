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
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.MultiPart;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * <p>Unit tests for {@link MultiPartReader} (in the client) and
 * {@link MultiPartWriter} (in the server).</p>
 */
public class MultiPartReaderWriterTest extends AbstractGrizzlyServerTester {
    
    public MultiPartReaderWriterTest(String testName) {
        super(testName);
    }

    Client client = null;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        startServer(MultiPartResource.class, MultiPartBeanProvider.class);

        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(MultiPartBeanProvider.class);
        client = Client.create(config);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        client = null;
    }


    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    public void testZero() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/zero").accept("text/plain");
        try {
            String result = builder.get(String.class);
            assertEquals("Hello, world\r\n", result);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    public void testOne() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/one").accept("multipart/mixed");
        try {
            MultiPart result = builder.get(MultiPart.class);
            checkMediaType(new MediaType("multipart", "mixed"), result.getMediaType());
            assertEquals(1, result.getBodyParts().size());
            BodyPart part = result.getBodyParts().get(0);
            checkMediaType(new MediaType("text", "plain"), part.getMediaType());
            checkEntity("This is the only segment", (BodyPartEntity) part.getEntity());

            result.getParameterizedHeaders();
            result.cleanup();
        } catch (IOException e) {
            e.printStackTrace(System.out);
            fail("Caught exception: " + e);
        } catch(ParseException e) {
            e.printStackTrace(System.out);
            fail("Caught exception: " + e);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    public void testTwo() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/two").accept("multipart/mixed");
        try {
            MultiPart result = builder.get(MultiPart.class);
            checkMediaType(new MediaType("multipart", "mixed"), result.getMediaType());
            assertEquals(2, result.getBodyParts().size());
            BodyPart part1 = result.getBodyParts().get(0);
            checkMediaType(new MediaType("text", "plain"), part1.getMediaType());
            checkEntity("This is the first segment", (BodyPartEntity) part1.getEntity());
            BodyPart part2 = result.getBodyParts().get(1);
            checkMediaType(new MediaType("text", "xml"), part2.getMediaType());
            checkEntity("<outer><inner>value</inner></outer>", (BodyPartEntity) part2.getEntity());

            result.getParameterizedHeaders();
            result.cleanup();
        } catch (IOException e) {
            e.printStackTrace(System.out);
            fail("Caught exception: " + e);
        } catch(ParseException e) {
            e.printStackTrace(System.out);
            fail("Caught exception: " + e);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    public void testThree() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/three").accept("multipart/mixed");
        try {
            MultiPart result = builder.get(MultiPart.class);
            checkMediaType(new MediaType("multipart", "mixed"), result.getMediaType());
            assertEquals(2, result.getBodyParts().size());
            BodyPart part1 = result.getBodyParts().get(0);
            checkMediaType(new MediaType("text", "plain"), part1.getMediaType());
            checkEntity("This is the first segment", (BodyPartEntity) part1.getEntity());
            BodyPart part2 = result.getBodyParts().get(1);
            checkMediaType(new MediaType("x-application", "x-format"), part2.getMediaType());
            MultiPartBean entity = part2.getEntityAs(MultiPartBean.class);
            assertEquals("myname", entity.getName());
            assertEquals("myvalue", entity.getValue());

            result.getParameterizedHeaders();
            result.cleanup();
        } catch (IOException e) {
            e.printStackTrace(System.out);
            fail("Caught exception: " + e);
        } catch(ParseException e) {
            e.printStackTrace(System.out);
            fail("Caught exception: " + e);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    public void testFour() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/four").accept("text/plain").type("multipart/mixed");
        try {
            MultiPartBean bean = new MultiPartBean("myname", "myvalue");
            MultiPart entity = new MultiPart().
              bodyPart("This is the first segment", new MediaType("text", "plain")).
              bodyPart(bean, new MediaType("x-application", "x-format"));
            String response = builder.put(String.class, entity);
            if (!response.startsWith("SUCCESS:")) {
                fail("Response is '" + response + "'");
            }
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    // Test sending a completely empty MultiPart
    public void testSix() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/six").type("multipart/mixed").accept("text/plain");
        try {
            String result = builder.post(String.class, new MultiPart());
            fail("Should have thrown an exception about zero body parts");
        } catch (ClientHandlerException e) {
            assertNotNull(e.getCause());
            assertEquals(WebApplicationException.class, e.getCause().getClass());
            WebApplicationException wae = (WebApplicationException) e.getCause();
            assertNotNull(wae.getCause());
            assertEquals(IllegalArgumentException.class, wae.getCause().getClass());
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    // Zero length body part
    public void testTen() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/ten").accept("text/plain").type("multipart/mixed");
        try {
            MultiPartBean bean = new MultiPartBean("myname", "myvalue");
            MultiPart entity = new MultiPart().
                    bodyPart(bean, new MediaType("x-application", "x-format")).
                    bodyPart("", MediaType.APPLICATION_OCTET_STREAM_TYPE);
            String response = builder.put(String.class, entity);
            if (!response.startsWith("SUCCESS:")) {
                fail("Response is '" + response + "'");
            }
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    // Echo back various sized body part entities, and check size/content
    public void testEleven() {
        String seed = "0123456789ABCDEF";
        checkEleven(seed, 0);
        checkEleven(seed, 1);
        checkEleven(seed, 10);
        checkEleven(seed, 100);
        checkEleven(seed, 1000);
        checkEleven(seed, 10000);
        checkEleven(seed, 100000);
    }

    // Echo back the multipart that was sent
    public void testTwelve() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/twelve").accept("multipart/mixed").type("multipart/mixed");
        try {
            MultiPart entity = new MultiPart().
              bodyPart("CONTENT", MediaType.TEXT_PLAIN_TYPE);
            MultiPart response = builder.put(MultiPart.class, entity);
            String actual = response.getBodyParts().get(0).getEntityAs(String.class);
            assertEquals("CONTENT", actual);
            response.cleanup();
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }

    }

    // Call clean up explicitly
    public void testThirteen() {
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/thirteen").accept("multipart/mixed").type("multipart/mixed");
        try {
            MultiPart entity = new MultiPart().
              bodyPart("CONTENT", MediaType.TEXT_PLAIN_TYPE);
            String response = builder.put(String.class, entity);
            assertEquals("cleanup", response);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }

    }

    /*
    public void testListen() throws Exception {
        System.out.println("Running for 30 seconds");
        Thread.sleep(30000);
    }
*/

    private void checkEntity(String expected, BodyPartEntity entity) throws IOException {
        // Convert the raw bytes into a String
        InputStreamReader sr = new InputStreamReader(entity.getInputStream());
        StringWriter sw = new StringWriter();
        while (true) {
            int ch = sr.read();
            if (ch < 0) {
                break;
            }
            sw.append((char) ch);
        }
        // Perform the comparison
        assertEquals(expected, sw.toString());
    }

    private void checkEleven(String seed, int multiplier) {
        StringBuilder sb = new StringBuilder(seed.length() * multiplier);
        for (int i = 0; i < multiplier; i++) {
            sb.append(seed);
        }
        String expected = sb.toString();
        WebResource.Builder builder = client.resource(getUri())
                .path("multipart/eleven").accept("multipart/mixed").type("multipart/mixed");
        try {
            MultiPart entity = new MultiPart().
              bodyPart(expected, MediaType.TEXT_PLAIN_TYPE);
            MultiPart response = builder.put(MultiPart.class, entity);
            String actual = response.getBodyParts().get(0).getEntityAs(String.class);
            assertEquals("Length for multiplier " + multiplier, expected.length(), actual.length());
            assertEquals("Content for multiplier " + multiplier, expected, actual);
            response.cleanup();
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e + " for multiplier " + multiplier);
        }
    }

    private void checkMediaType(MediaType expected, MediaType actual) {
        assertEquals("Expected MediaType=" + expected, expected.getType(), actual.getType());
        assertEquals("Expected MediaType=" + expected, expected.getSubtype(), actual.getSubtype());
    }

    private void report(UniformInterfaceException e) {
        System.out.println("Got UniformInterfaceException: " + e.getMessage());
        e.printStackTrace(System.out);
        ClientResponse r = e.getResponse();
        System.out.println("Response:");
        System.out.println("  Location=" + r.getLocation());
        System.out.println("  Status=" + r.getStatus());
        MultivaluedMap<String,String> metadata = r.getMetadata();
        for (Map.Entry<String,List<String>> entry : metadata.entrySet()) {
            for (String value : entry.getValue()) {
                System.out.println("  Header=" + entry.getKey() + ", Value=" + value);
            }
        }
    }

}

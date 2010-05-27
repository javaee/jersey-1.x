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
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.multipart.MultiPart;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FormDataMultiPartReaderWriterTest extends AbstractGrizzlyServerTester {

    public FormDataMultiPartReaderWriterTest(String testName) {
        super(testName);
    }

    Client client = null;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        ClientConfig config = new DefaultClientConfig();
        config.getClasses().add(MultiPartBeanProvider.class);
        client = Client.create(config);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        client = null;
    }


    @Path("/")
    public static class ProducesFormDataUsingMultiPart {

        @GET
        @Produces("multipart/form-data")
        public Response get() {
            MultiPart entity = new MultiPart();
            entity.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
            BodyPart part1 = new BodyPart();
            part1.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            part1.getHeaders().add("Content-Disposition", "form-data; name=\"field1\"");
            part1.setEntity("Joe Blow\r\n");
            BodyPart part2 = new BodyPart();
            part2.setMediaType(MediaType.TEXT_PLAIN_TYPE);
            part2.getHeaders().add("Content-Disposition", "form-data; name=\"pics\"; filename=\"file1.txt\"");
            part2.setEntity("... contents of file1.txt ...\r\n");
            return Response.ok(entity.bodyPart(part1).bodyPart(part2)).build();
        }

    }

    // Test a response of type "multipart/form-data".  The example comes from
    // Section 6 of RFC 1867.
    public void testProducesFormDataUsingMultiPart() {
        startServer(ProducesFormDataUsingMultiPart.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("multipart/form-data");
        try {
            MultiPart result = builder.get(MultiPart.class);
            checkMediaType(new MediaType("multipart", "form-data"), result.getMediaType());
            assertEquals(2, result.getBodyParts().size());
            BodyPart part1 = result.getBodyParts().get(0);
            checkMediaType(new MediaType("text", "plain"), part1.getMediaType());
            checkEntity("Joe Blow\r\n", (BodyPartEntity) part1.getEntity());
            String value1 = part1.getHeaders().getFirst("Content-Disposition");
            assertEquals("form-data; name=\"field1\"", value1);
            BodyPart part2 = result.getBodyParts().get(1);
            checkMediaType(new MediaType("text", "plain"), part2.getMediaType());
            checkEntity("... contents of file1.txt ...\r\n", (BodyPartEntity) part2.getEntity());
            String value2 = part2.getHeaders().getFirst("Content-Disposition");
            assertEquals("form-data; name=\"pics\"; filename=\"file1.txt\"", value2);

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


    @Path("/")
    public static class ProducesFormDataResource {

        // Test "multipart/form-data" the easy way (with subclasses)
        @GET
        @Produces("multipart/form-data")
        public Response get() {
            // Exercise builder pattern with explicit content type
            MultiPartBean bean = new MultiPartBean("myname", "myvalue");
            return Response.ok(new FormDataMultiPart().
                                 field("foo", "bar").
                                 field("baz", "bop").
                                 field("bean", bean, new MediaType("x-application", "x-format"))).build();
        }
    }

    public void testProducesFormDataResource() {
        startServer(ProducesFormDataResource.class, MultiPartBeanProvider.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("multipart/form-data");
        try {
            FormDataMultiPart result = builder.get(FormDataMultiPart.class);
            checkMediaType(new MediaType("multipart", "form-data"), result.getMediaType());
            assertEquals(3, result.getFields().size());
            assertNotNull(result.getField("foo"));
            assertEquals("bar", result.getField("foo").getValue());
            assertNotNull(result.getField("baz"));
            assertEquals("bop", result.getField("baz").getValue());
            assertNotNull(result.getField("bean"));
            MultiPartBean bean = result.getField("bean").getValueAs(MultiPartBean.class);
            assertNotNull(bean);
            assertEquals("myname", bean.getName());
            assertEquals("myvalue", bean.getValue());
            result.cleanup();
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class ProducesFormDataCharsetResource {

        // Test "multipart/form-data" the easy way (with subclasses)
        @GET
        @Produces("multipart/form-data")
        public Response get(@QueryParam("charset") String charset) {
            return Response.ok(new FormDataMultiPart().
                                 field("foo", "\u00A9 CONTENT \u00FF \u2200 \u22FF",
                                 MediaType.valueOf("text/plain;charset=" + charset))).build();
        }
    }
    
    public void testProducesFormDataCharsetResource() throws Exception {
        startServer(ProducesFormDataCharsetResource.class);

        client.addFilter(new LoggingFilter());
        String c = "\u00A9 CONTENT \u00FF \u2200 \u22FF";
        for (String charset : Arrays.asList(
                "US-ASCII",
                "ISO-8859-1",
                "UTF-8",
                "UTF-16BE",
                "UTF-16LE",
                "UTF-16")) {
            FormDataMultiPart p = client.resource(getUri()).path("/").
                    queryParam("charset", charset).
                    accept("multipart/form-data").
                    get(FormDataMultiPart.class);

            String expected = new String(c.getBytes(charset), charset);
            assertEquals(expected, p.getField("foo").getValue());
        }
    }

    @Path("/")
    public static class ConsumesFormDataResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public Response get(FormDataMultiPart multiPart) {
            if (!(multiPart.getBodyParts().size() == 3)) {
                return Response.ok("FAILED:  Number of body parts is " + multiPart.getBodyParts().size() + " instead of 3").build();
            }
            if (multiPart.getField("foo") == null) {
                return Response.ok("FAILED:  Missing field 'foo'").build();
            } else if (!"bar".equals(multiPart.getField("foo").getValue())) {
                return Response.ok("FAILED:  Field 'foo' has value '" + multiPart.getField("foo").getValue() + "' instead of 'bar'").build();
            }
            if (multiPart.getField("baz") == null) {
                return Response.ok("FAILED:  Missing field 'baz'").build();
            } else if (!"bop".equals(multiPart.getField("baz").getValue())) {
                return Response.ok("FAILED:  Field 'baz' has value '" + multiPart.getField("baz").getValue() + "' instead of 'bop'").build();
            }
            if (multiPart.getField("bean") == null) {
                return Response.ok("FAILED:  Missing field 'bean'").build();
            }
            MultiPartBean bean = multiPart.getField("bean").getValueAs(MultiPartBean.class);
            if (!bean.getName().equals("myname")) {
                return Response.ok("FAILED:  Second part name = " + bean.getName()).build();
            }
            if (!bean.getValue().equals("myvalue")) {
                return Response.ok("FAILED:  Second part value = " + bean.getValue()).build();
            }
            return Response.ok("SUCCESS:  All tests passed").build();
        }
    }

    public void testConsumesFormDataResource() {
        startServer(ConsumesFormDataResource.class, MultiPartBeanProvider.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data");
        try {
            MultiPartBean bean = new MultiPartBean("myname", "myvalue");
            FormDataMultiPart entity = new FormDataMultiPart().
                field("foo", "bar").
                field("baz", "bop").
                field("bean", bean, new MediaType("x-application", "x-format"));
            String response = builder.put(String.class, entity);
            if (!response.startsWith("SUCCESS:")) {
                fail("Response is '" + response + "'");
            }
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class ConsumesFormDataParamResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public Response get(
                @FormDataParam("foo") String foo,
                @FormDataParam("baz") String baz,
                @FormDataParam("bean") MultiPartBean bean,
                @FormDataParam("unknown1") String unknown1,
                @FormDataParam("unknown2") @DefaultValue("UNKNOWN") String unknown2,
                FormDataMultiPart fdmp) {

            if (!"bar".equals(foo)) {
                return Response.ok("FAILED:  Value of 'foo' is '" + foo + "' instead of 'bar'").build();
            } else if (!"bop".equals(baz)) {
                return Response.ok("FAILED:  Value of 'baz' is '" + baz + "' instead of 'bop'").build();
            } else if (bean == null) {
                return Response.ok("FAILED:  Value of 'bean' is NULL").build();
            } else if (!(bean.getName().equals("myname") && bean.getValue().equals("myvalue"))) {
                return Response.ok("FAILED:  Value of 'bean.myName' and 'bean.MyValue' are not 'myname' and 'myvalue'").build();
            } else if (unknown1 != null) {
                return Response.ok("FAILED:  Value of 'unknown1' is '" + unknown1 + "' instead of NULL").build();
            } else if (!"UNKNOWN".equals(unknown2)) {
                return Response.ok("FAILED:  Value of 'unknown2' is '" + unknown2 + "' instead of 'UNKNOWN'").build();
            } else if (fdmp == null) {
                return Response.ok("FAILED:  Value of fdmp is NULL").build();
            } else if (fdmp.getFields().size() != 3) {
                return Response.ok("FAILED:  Value of fdmp.getFields().size() is " + fdmp.getFields().size() + " instead of 3").build();
            }
            return Response.ok("SUCCESS:  All tests passed").build();
        }
    }

    public void testConsumesFormDataParamResource() {
        startServer(ConsumesFormDataParamResource.class, MultiPartBeanProvider.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data");
        try {
            MultiPartBean bean = new MultiPartBean("myname", "myvalue");
            FormDataMultiPart entity = new FormDataMultiPart().
                field("foo", "bar").
                field("baz", "bop").
                field("bean", bean, new MediaType("x-application", "x-format"));
            String response = builder.put(String.class, entity);
            if (!response.startsWith("SUCCESS:")) {
                fail("Response is '" + response + "'");
            }
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class FormDataTypesResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public String get(
                @FormDataParam("foo") FormDataContentDisposition fooDisp,
                @FormDataParam("foo") FormDataBodyPart fooPart,
                @FormDataParam("baz") FormDataContentDisposition bazDisp,
                @FormDataParam("baz") FormDataBodyPart bazPart) {

            assertNotNull(fooDisp);
            assertNotNull(fooPart);
            assertNotNull(bazDisp);
            assertNotNull(bazPart);

            assertEquals("foo", fooDisp.getName());
            assertEquals("foo", fooPart.getName());
            assertEquals("bar", fooPart.getValue());

            assertEquals("baz", bazDisp.getName());
            assertEquals("baz", bazPart.getName());
            assertEquals("bop", bazPart.getValue());

            return "OK";
        }
    }

    public void testFormDataTypesResource() {
        startServer(FormDataTypesResource.class, MultiPartBeanProvider.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data");
        try {
            FormDataMultiPart entity = new FormDataMultiPart().
                field("foo", "bar").
                field("baz", "bop");
            String response = builder.put(String.class, entity);
            assertEquals("OK", response);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class FormDataListTypesResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public String get(
                @FormDataParam("foo") List<FormDataContentDisposition> fooDisp,
                @FormDataParam("foo") List<FormDataBodyPart> fooPart,
                @FormDataParam("baz") List<FormDataContentDisposition> bazDisp,
                @FormDataParam("baz") List<FormDataBodyPart> bazPart) {

            assertNotNull(fooDisp);
            assertNotNull(fooPart);
            assertNotNull(bazDisp);
            assertNotNull(bazPart);

            assertEquals(2, fooDisp.size());
            assertEquals(2, fooPart.size());
            assertEquals(2, bazDisp.size());
            assertEquals(2, bazPart.size());

            return "OK";
        }
    }

    public void testFormDataListTypesResource() {
        startServer(FormDataListTypesResource.class, MultiPartBeanProvider.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data");
        try {
            FormDataMultiPart entity = new FormDataMultiPart().
                field("foo", "bar").
                field("foo", "bar2").
                field("baz", "bop").
                field("baz", "bop2");
            String response = builder.put(String.class, entity);
            assertEquals("OK", response);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class FormDataCollectionTypesResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public String get(
                @FormDataParam("foo") Collection<FormDataContentDisposition> fooDisp,
                @FormDataParam("foo") Collection<FormDataBodyPart> fooPart,
                @FormDataParam("baz") Collection<FormDataContentDisposition> bazDisp,
                @FormDataParam("baz") Collection<FormDataBodyPart> bazPart) {

            assertNotNull(fooDisp);
            assertNotNull(fooPart);
            assertNotNull(bazDisp);
            assertNotNull(bazPart);

            assertEquals(2, fooDisp.size());
            assertEquals(2, fooPart.size());
            assertEquals(2, bazDisp.size());
            assertEquals(2, bazPart.size());

            return "OK";
        }
    }

    public void testFormDataCollectionTypesResource() {
        startServer(FormDataCollectionTypesResource.class, MultiPartBeanProvider.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data");
        try {
            FormDataMultiPart entity = new FormDataMultiPart().
                field("foo", "bar").
                field("foo", "bar2").
                field("baz", "bop").
                field("baz", "bop2");
            String response = builder.put(String.class, entity);
            assertEquals("OK", response);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class PrimitivesFormDataParamResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public String get(
                @FormDataParam("bP") boolean bP,
                @FormDataParam("bT") Boolean bT,
                @FormDataParam("bP_absent") boolean bP_absent,
                @FormDataParam("bT_absent") Boolean bT_absent,
                @DefaultValue("true") @FormDataParam("bP_absent_default") boolean bP_absent_default,
                @DefaultValue("true") @FormDataParam("bT_absent_default") Boolean bT_absent_default,
                @DefaultValue("true") @FormDataParam("bP_default") boolean bP_default,
                @DefaultValue("true") @FormDataParam("bT_default") Boolean bT_default
                ) {
            assertTrue(bP);
            assertTrue(bT);
            assertFalse(bP_absent);
            assertNull(bT_absent);
            assertTrue(bP_absent_default);
            assertTrue(bT_absent_default);
            assertFalse(bP_default);
            assertFalse(bT_default);

            return "OK";
        }
    }

    public void testPrimitivesFormDataParamResource() {
        startServer(PrimitivesFormDataParamResource.class, MultiPartBeanProvider.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data");
        try {
            FormDataMultiPart entity = new FormDataMultiPart().
                field("bP", "true").
                field("bT", "true").
                field("bP_default", "false").
                field("bT_default", "false");
            String response = builder.put(String.class, entity);
            assertEquals("OK", response);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class DefaultFormDataParamResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public String get(
                @FormDataParam("bean") MultiPartBean bean,
                @FormDataParam("bean_absent") MultiPartBean bean_absent,
                @DefaultValue("myname=myvalue") @FormDataParam("bean_default") MultiPartBean bean_default
                ) {
            assertNotNull(bean);
            assertNull(bean_absent);
            assertNull(bean_default);

            assertEquals("myname", bean.getName());
            assertEquals("myvalue", bean.getValue());

            return "OK";
        }
    }

    public void testDefaultFormDataParamResource() {
        startServer(DefaultFormDataParamResource.class, MultiPartBeanProvider.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data");
        try {
            MultiPartBean bean = new MultiPartBean("myname", "myvalue");
            FormDataMultiPart entity = new FormDataMultiPart().
                field("bean", bean, new MediaType("x-application", "x-format"));
            String response = builder.put(String.class, entity);
            assertEquals("OK", response);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class NonContentTypeForPartResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public String put(
                @FormDataParam("submit") FormDataBodyPart bean
                ) {
            assertNotNull(bean);
            assertNull(bean.getHeaders().getFirst("Content-Type"));
            assertEquals("upload", bean.getValue());
            return "OK";
        }
    }

    public void testNonContentTypeForPartResource() {
        startServer(NonContentTypeForPartResource.class);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type("multipart/form-data;boundary=\"---------------------------33219615019106944971719437488\"");
        try {
            String entity =
                    "-----------------------------33219615019106944971719437488\n" +
                    "Content-Disposition: form-data; name=\"submit\"\n\n" +
                    "upload\n" +
                    "-----------------------------33219615019106944971719437488--";
            String response = builder.put(String.class, entity);
            assertEquals("OK", response);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }

    @Path("/")
    public static class MediaTypeWithBoundaryResource {

        @PUT
        @Consumes("multipart/form-data")
        @Produces("text/plain")
        public String get(
                @Context HttpHeaders h,
                @FormDataParam("submit") String s
                ) {
            String b = h.getMediaType().getParameters().get("boundary");
            assertEquals("XXXX_YYYY", b);
            return s;
        }
    }

    public void testMediaTypeWithBoundaryResource() {
        startServer(MediaTypeWithBoundaryResource.class);

        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("boundary", "XXXX_YYYY");
        MediaType mediaType = new MediaType(
                MediaType.MULTIPART_FORM_DATA_TYPE.getType(),
                MediaType.MULTIPART_FORM_DATA_TYPE.getSubtype(), parameters);

        WebResource.Builder builder = client.resource(getUri()).
                accept("text/plain").type(mediaType);
        try {

            FormDataMultiPart entity = new FormDataMultiPart().
                    field("submit", "OK");
            String response = builder.put(String.class, entity);
            assertEquals("OK", response);
        } catch (UniformInterfaceException e) {
            report(e);
            fail("Caught exception: " + e);
        }
    }




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

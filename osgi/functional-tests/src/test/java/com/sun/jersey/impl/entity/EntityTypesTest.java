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

package com.sun.jersey.impl.entity;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.atom.rome.impl.provider.entity.AtomEntryProvider;
import com.sun.jersey.atom.rome.impl.provider.entity.AtomFeedProvider;
import com.sun.jersey.core.impl.provider.entity.FileProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.activation.DataSource;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.junit.Assert.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EntityTypesTest extends AbstractTypeTester {
    
    @Path("/")
    public static class InputStreamResource {
        @POST
        public InputStream post(InputStream in) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read;
            final byte[] data = new byte[2048];
            while ((read = in.read(data)) != -1)
                out.write(data, 0, read);
            
            return new ByteArrayInputStream(out.toByteArray());
        }                
    }

    @Test
    public void testInputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream("CONTENT".getBytes());
        _test(in, InputStreamResource.class);
    }
    
    @Path("/")
    public static class StringResource extends AResource<String> {}

    @Test
    public void testString() {
        _test("CONTENT", StringResource.class);
    }

    @Path("/")
    public static class DataSourceResource extends AResource<DataSource> {}

    @Test
    public void testDataSource() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream("CONTENT".getBytes());
        ByteArrayDataSource ds = new ByteArrayDataSource(bais, "text/plain");        
        _test(ds, DataSourceResource.class);
    }
    
    @Path("/")
    public static class ByteArrayResource extends AResource<byte[]> {}

    @Test
    public void testByteArrayRepresentation() {
        _test("CONTENT".getBytes(), ByteArrayResource.class);
    }
    
    @Path("/")
    @Produces("application/xml")
    @Consumes("application/xml")
    public static class JAXBBeanResource extends AResource<JAXBBean> {}
    
    public void testJAXBBeanRepresentation() {
        _test(new JAXBBean("CONTENT"), JAXBBeanResource.class, MediaType.APPLICATION_XML_TYPE);
    }
    
    @Path("/")
    @Produces("application/foo+xml")
    @Consumes("application/foo+xml")
    public static class JAXBBeanResourceMediaType extends AResource<JAXBBean> {}

    @Test
    public void testJAXBBeanRepresentationMediaType() {
        _test(new JAXBBean("CONTENT"), JAXBBeanResourceMediaType.class, MediaType.valueOf("application/foo+xml"));
    }

    @Test
    public void testJAXBBeanRepresentationError() {
        initiateWebApplication(JAXBBeanResource.class);
        WebResource r = resource("/", false);

        String xml = "<root>foo</root>";
        ClientResponse cr = r.type("application/xml").post(ClientResponse.class, xml);
        assertEquals(400, cr.getStatus());
    }

    @Path("/")
    @Produces("text/xml")
    @Consumes("text/xml")
    public static class JAXBBeanTextResource extends AResource<JAXBBean> {}
    
    @Test
    public void testJAXBBeanTextRepresentation() {
        _test(new JAXBBean("CONTENT"), JAXBBeanTextResource.class, MediaType.TEXT_XML_TYPE);
    }
    
    @Path("/")
    @Produces("application/xml")
    @Consumes("application/xml")
    public static class JAXBElementBeanResource extends AResource<JAXBElement<JAXBBeanType>> {}
    
    @Test
    public void testJAXBElementBeanRepresentation() {
        _test(new JAXBBean("CONTENT"), JAXBElementBeanResource.class, MediaType.APPLICATION_XML_TYPE);
    }
    
    @Path("/")
    @Produces("application/foo+xml")
    @Consumes("application/foo+xml")
    public static class JAXBElementBeanResourceMediaType extends AResource<JAXBElement<JAXBBeanType>> {}

    @Test
    public void testJAXBElementBeanRepresentationMediaType() {
        _test(new JAXBBean("CONTENT"), JAXBElementBeanResourceMediaType.class, MediaType.valueOf("application/foo+xml"));
    }

    @Test
    public void testJAXBElementBeanRepresentationError() {
        initiateWebApplication(JAXBElementBeanResource.class);
        WebResource r = resource("/", false);

        String xml = "<root><value>foo";
        ClientResponse cr = r.type("application/xml").post(ClientResponse.class, xml);
        assertEquals(400, cr.getStatus());
    }

    @Path("/")
    @Produces("text/xml")
    @Consumes("text/xml")
    public static class JAXBElementBeanTextResource extends AResource<JAXBElement<JAXBBeanType>> {}
    
    @Test
    public void testJAXBElementBeanTextRepresentation() {
        _test(new JAXBBean("CONTENT"), JAXBElementBeanTextResource.class, MediaType.TEXT_XML_TYPE);
    }
    
    @Path("/")
    @Produces("application/xml")
    @Consumes("application/xml")
    public static class JAXBTypeResource {
        @POST
        public JAXBBean post(JAXBBeanType t) {
            return new JAXBBean(t.value);
        }
    }
    
    @Test
    public void testJAXBTypeRepresentation() {
        initiateWebApplication(JAXBTypeResource.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBeanType out = r.entity(in, "application/xml").
                post(JAXBBeanType.class);
        assertEquals(in.value, out.value);
    }
    
    @Path("/")
    @Produces("application/foo+xml")
    @Consumes("application/foo+xml")
    public static class JAXBTypeResourceMediaType extends JAXBTypeResource  {
    }

    @Test
    public void testJAXBTypeRepresentationMediaType() {
        initiateWebApplication(JAXBTypeResourceMediaType.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBeanType out = r.entity(in, "application/foo+xml").
                post(JAXBBeanType.class);
        assertEquals(in.value, out.value);
    }
    
    
    @Path("/")
    @Produces("application/xml")
    @Consumes("application/xml")
    public static class JAXBObjectResource {
        @POST
        public Object post(Object o) {
            return o;
        }
    }
    
    @Provider
    public static class JAXBObjectResolver implements  ContextResolver<JAXBContext> {
        public JAXBContext getContext(Class<?> c) {
            if (Object.class == c) {
                try {
                    return JAXBContext.newInstance(JAXBBean.class);
                } catch (JAXBException ex) {
                }
            }
            return null;
        }
    }
    
    @Test
    public void testJAXBObjectRepresentation() {
        initiateWebApplication(JAXBObjectResolver.class, JAXBObjectResource.class);
        WebResource r = resource("/");
        Object in = new JAXBBean("CONTENT");
        JAXBBean out = r.entity(in, "application/xml").
                post(JAXBBean.class);
        assertEquals(in, out);
    }

    @Path("/")
    @Produces("application/foo+xml")
    @Consumes("application/foo+xml")
    public static class JAXBObjectResourceMediaType extends JAXBObjectResource {
    }

    @Test
    public void testJAXBObjectRepresentationMediaType() {
        initiateWebApplication(JAXBObjectResolver.class, JAXBObjectResourceMediaType.class);
        WebResource r = resource("/");
        Object in = new JAXBBean("CONTENT");
        JAXBBean out = r.entity(in, "application/foo+xml").
                post(JAXBBean.class);
        assertEquals(in, out);
    }


    @Test
    public void testJAXBObjectRepresentationError() {
        initiateWebApplication(JAXBObjectResolver.class, JAXBObjectResource.class);
        WebResource r = resource("/", false);

        String xml = "<root>foo</root>";
        ClientResponse cr = r.type("application/xml").post(ClientResponse.class, xml);
        assertEquals(400, cr.getStatus());
    }
    
    @Path("/")
    public static class FileResource extends AResource<File> {}
    
    @Test
    public void testFileRepresentation() throws IOException {
        FileProvider fp = new FileProvider();
        File in = fp.readFrom(File.class, File.class, null, null, null, 
                new ByteArrayInputStream("CONTENT".getBytes()));
        
        _test(in, FileResource.class);
    }
    
    @Path("/")
    public static class MimeMultipartBeanResource extends AResource<MimeMultipart> {}
    
    @Test
    public void testMimeMultipartRepresentation() throws Exception {
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("content-disposition", "form-data; name=\"field1\"");
        MimeMultipart mmIn = new MimeMultipart();
        MimeBodyPart bp = new MimeBodyPart(headers, "Joe Blow".getBytes());
        mmIn.addBodyPart(bp);

        InternetHeaders headers2 = new InternetHeaders();
        headers2.addHeader("content-disposition", "form-data; name=\"field2\"");
        bp = new MimeBodyPart(headers2, "Jane Doe".getBytes());
        mmIn.addBodyPart(bp);

        InternetHeaders headers3 = new InternetHeaders();
        headers3.addHeader("content-disposition", "form-data; name=\"pic\"; filename=\"duke_rocket.gif\"");
        headers3.addHeader("Content-type", "image/gif");
        headers3.addHeader("Content-Transfer-Encoding", "binary");
        
        InputStream fs = this.getClass().getResourceAsStream("duke_rocket.gif");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int l;
        while( (l = fs.read(buffer)) != -1) {
            outputStream.write(buffer, 0, l);
        }
        outputStream.close();
        
        bp = new MimeBodyPart(headers3, outputStream.toByteArray());
        mmIn.addBodyPart(bp);
        _test(mmIn, MimeMultipartBeanResource.class, false);
    }
    
    @Produces("application/x-www-form-urlencoded")
    @Consumes("application/x-www-form-urlencoded")
    @Path("/")
    public static class FormResource extends AResource<Form> {}
    
    public void ignoredTestFormRepresentation() {
        Form fp = new Form();
        fp.add("Email", "johndoe@gmail.com");
        fp.add("Passwd", "north 23AZ");
        fp.add("service", "cl");
        fp.add("source", "Gulp-CalGul-1.05");
        
        _test(fp, FormResource.class);
    }
    
   
    @Produces("application/json")
    @Consumes("application/json")
    @Path("/")
    public static class JSONObjectResource extends AResource<JSONObject> {}

    @Test
    public void testJSONObjectRepresentation() throws Exception {
        JSONObject object = new JSONObject();
        object.put("userid", 1234).
        put("username", "1234").
        put("email", "a@b").
        put("password", "****");

        _test(object, JSONObjectResource.class, MediaType.APPLICATION_JSON_TYPE);
    }

    @Produces("application/xxx+json")
    @Consumes("application/xxx+json")
    @Path("/")
    public static class JSONObjectResourceGeneralMediaType extends AResource<JSONObject> {}

    @Test
    public void testJSONObjectRepresentationGeneralMediaTyp() throws Exception {
        JSONObject object = new JSONObject();
        object.put("userid", 1234).
        put("username", "1234").
        put("email", "a@b").
        put("password", "****");

        _test(object, JSONObjectResourceGeneralMediaType.class, MediaType.valueOf("application/xxx+json"));
    }

    @Produces("application/json")
    @Consumes("application/json")
    @Path("/")
    public static class JSONOArrayResource extends AResource<JSONArray> {}

    @Test
    public void testJSONArrayRepresentation() throws Exception {
        JSONArray array = new JSONArray();
        array.put("One").put("Two").put("Three").put(1).put(2.0);

        _test(array, JSONOArrayResource.class, MediaType.APPLICATION_JSON_TYPE);
    }

    @Produces("application/xxx+json")
    @Consumes("application/xxx+json")
    @Path("/")
    public static class JSONOArrayResourceGeneralMediaType extends AResource<JSONArray> {}

    @Test
    public void testJSONArrayRepresentationGeneralMediaType() throws Exception {
        JSONArray array = new JSONArray();
        array.put("One").put("Two").put("Three").put(1).put(2.0);

        _test(array, JSONOArrayResourceGeneralMediaType.class, MediaType.valueOf("application/xxx+json"));
    }

    @Path("/")
    public static class FeedResource extends AResource<Feed> {}
    
    @Test
    public void testFeedRepresentation() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("feed.xml");
        AtomFeedProvider afp = new AtomFeedProvider();
        Feed f = afp.readFrom(Feed.class, Feed.class, null, null, null, in);
        
        _test(f, FeedResource.class);
    }
    
    @Path("/")
    public static class EntryResource extends AResource<Entry> {}
    
    @Test
    public void testEntryRepresentation() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("entry.xml");        
        AtomEntryProvider afp = new AtomEntryProvider();
        Entry e = afp.readFrom(Entry.class, Entry.class, null, null, null, in);
        
        _test(e, EntryResource.class);
    }
    
    @Path("/")
    public static class ReaderResource extends AResource<Reader> {}
    
    @Test
    public void testReaderRepresentation() throws Exception {        
        _test(new StringReader("CONTENT"), ReaderResource.class);
    }
    
    private final static String XML_DOCUMENT = "<n:x xmlns:n=\"urn:n\"><n:e>CONTNET</n:e></n:x>";
    
    @Path("/")
    public static class StreamSourceResource extends AResource<StreamSource> {}
    
    @Test
    public void testStreamSourceRepresentation() throws Exception {
        StreamSource ss = new StreamSource(
                new ByteArrayInputStream(XML_DOCUMENT.getBytes()));
        _test(ss, StreamSourceResource.class);
    }
    
    @Path("/")
    public static class SAXSourceResource extends AResource<SAXSource> {}
    
    @Test
    public void testSAXSourceRepresentation() throws Exception {
        StreamSource ss = new StreamSource(
                new ByteArrayInputStream(XML_DOCUMENT.getBytes()));
        _test(ss, SAXSourceResource.class);
    }
    
    @Path("/")
    public static class DOMSourceResource extends AResource<DOMSource> {}
    
    @Test
    public void testDOMSourceRepresentation() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document d = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(XML_DOCUMENT)));
        DOMSource ds = new DOMSource(d);
        _test(ds, DOMSourceResource.class);
    }
    
    @Path("/")
    public static class DocumentResource extends AResource<Document> {}

    @Test
    public void testDocumentRepresentation() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document d = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(XML_DOCUMENT)));
        _test(d, DocumentResource.class);
    }

    @Path("/")
    @Produces("application/x-www-form-urlencoded")
    @Consumes("application/x-www-form-urlencoded")
    public static class FormMultivaluedMapResource {
        @POST
        public MultivaluedMap<String, String> post(MultivaluedMap<String, String> t) {
            return t;
        }
    }
    
    @Test
    public void testFormMultivaluedMapRepresentation() {
        MultivaluedMap<String, String> fp = new MultivaluedMapImpl();
        fp.add("Email", "johndoe@gmail.com");
        fp.add("Passwd", "north 23AZ");
        fp.add("service", "cl");
        fp.add("source", "Gulp-CalGul-1.05");
        fp.add("source", "foo.java");
        fp.add("source", "bar.java");
        
        initiateWebApplication(FormMultivaluedMapResource.class);
        WebResource r = resource("/");
        MultivaluedMap _fp = r.entity(fp, "application/x-www-form-urlencoded").
                post(MultivaluedMap.class);
        assertEquals(fp, _fp);
    }
    
    @Path("/")
    public static class StreamingOutputResource {
        @GET public StreamingOutput get() {
            return new StreamingOutput() {
                public void write(OutputStream entity) throws IOException {
                    entity.write(new String("CONTENT").getBytes());
                }
            };
        }
    }
    
    @Test
    public void testStreamingOutputRepresentation() throws Exception {
        initiateWebApplication(StreamingOutputResource.class);
        WebResource r = resource("/");
        assertEquals("CONTENT", r.get(String.class));
    }
    
    @Path("/")
    @Consumes("application/json")
    @Produces("application/json")
    public static class JAXBElementBeanJSONResource extends AResource<JAXBElement<String>> {}
    
    @Test
    public void testJAXBElementBeanJSONRepresentation() {
        initiateWebApplication(JAXBElementBeanJSONResource.class);
        WebResource r = resource("/");

        ClientResponse rib = r.type("application/json").
                post(ClientResponse.class, new JAXBElement<String>(new QName("test"), String.class, "CONTENT"));
        
        // TODO: the following would not be needed if i knew how to workaround JAXBElement<String>.class literal
        
        byte[] inBytes = (byte[])
                rib.getProperties().get("request.entity");
        byte[] outBytes = (byte[])
                rib.getProperties().get("response.entity");
        
        assertEquals(inBytes.length, outBytes.length);
        boolean e = false;
        for (int i = 0; i < inBytes.length; i++) {
            if (inBytes[i] != outBytes[i])
                assertEquals("Index: " + i, inBytes[i], outBytes[i]);
        }
    }    

    @Path("/")
    @Produces("application/json")
    @Consumes("application/json")
    public static class JAXBBeanResourceJSON extends AResource<JAXBBean> {}

    @Test
    public void testJAXBBeanRepresentationJSON() {
        initiateWebApplication(JAXBBeanResourceJSON.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBean out = r.entity(in, "application/json").
                post(JAXBBean.class);
        assertEquals(in.value, out.value);
    }

    @Path("/")
    @Produces("application/foo+json")
    @Consumes("application/foo+json")
    public static class JAXBBeanResourceJSONMediaType extends AResource<JAXBBean> {}

    @Test
    public void testJAXBBeanRepresentationJSONMediaType() {
        initiateWebApplication(JAXBBeanResourceJSONMediaType.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBean out = r.entity(in, "application/foo+json").
                post(JAXBBean.class);
        assertEquals(in.value, out.value);
    }

    @Path("/")
    @Produces("application/json")
    @Consumes("application/json")
    public static class JAXBElementBeanResourceJSON extends AResource<JAXBElement<JAXBBeanType>> {}

    @Test
    public void testJAXBElementBeanRepresentationJSON() {
        initiateWebApplication(JAXBElementBeanResourceJSON.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBean out = r.entity(in, "application/json").
                post(JAXBBean.class);
        assertEquals(in.value, out.value);
    }

    @Path("/")
    @Produces("application/foo+json")
    @Consumes("application/foo+json")
    public static class JAXBElementBeanResourceJSONMediaType extends AResource<JAXBElement<JAXBBeanType>> {}

    @Test
    public void testJAXBElementBeanRepresentationJSONMediaType() {
        initiateWebApplication(JAXBElementBeanResourceJSONMediaType.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBean out = r.entity(in, "application/foo+json").
                post(JAXBBean.class);
        assertEquals(in.value, out.value);
    }

    @Path("/")
    @Produces("application/json")
    @Consumes("application/json")
    public static class JAXBTypeResourceJSON {
        @POST
        public JAXBBean post(JAXBBeanType t) {
            return new JAXBBean(t.value);
        }
    }

    @Test
    public void testJAXBTypeRepresentationJSON() {
        initiateWebApplication(JAXBTypeResourceJSON.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBeanType out = r.entity(in, "application/json").
                post(JAXBBeanType.class);
        assertEquals(in.value, out.value);
    }
    
    @Path("/")
    @Produces("application/foo+json")
    @Consumes("application/foo+json")
    public static class JAXBTypeResourceJSONMediaType {
        @POST
        public JAXBBean post(JAXBBeanType t) {
            return new JAXBBean(t.value);
        }
    }

    @Test
    public void testJAXBTypeRepresentationJSONMediaType() {
        initiateWebApplication(JAXBTypeResourceJSONMediaType.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBeanType out = r.entity(in, "application/foo+json").
                post(JAXBBeanType.class);
        assertEquals(in.value, out.value);
    }
    
    @Path("/")
    @Produces("application/fastinfoset")
    @Consumes("application/fastinfoset")
    public static class JAXBBeanResourceFastInfoset extends AResource<JAXBBean> {}
    
    @Test
    public void testJAXBBeanRepresentationFastInfoset() {
        initiateWebApplication(JAXBBeanResourceFastInfoset.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBean out = r.entity(in, "application/fastinfoset").
                post(JAXBBean.class);
        assertEquals(in.value, out.value);
    }
    
    @Path("/")
    @Produces("application/fastinfoset")
    @Consumes("application/fastinfoset")
    public static class JAXBElementBeanResourceFastInfoset extends AResource<JAXBElement<JAXBBeanType>> {}

    @Test
    public void testJAXBElementBeanRepresentationFastInfoset() {
        initiateWebApplication(JAXBElementBeanResourceFastInfoset.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBean out = r.entity(in, "application/fastinfoset").
                post(JAXBBean.class);
        assertEquals(in.value, out.value);
    }
    
    @Path("/")
    @Produces("application/fastinfoset")
    @Consumes("application/fastinfoset")
    public static class JAXBTypeResourceFastInfoset {
        @POST
        public JAXBBean post(JAXBBeanType t) {
            return new JAXBBean(t.value);
        }
    }

    @Test
    public void testJAXBTypeRepresentationFastInfoset() {
        initiateWebApplication(JAXBTypeResourceFastInfoset.class);
        WebResource r = resource("/");
        JAXBBean in = new JAXBBean("CONTENT");
        JAXBBeanType out = r.entity(in, "application/fastinfoset").
                post(JAXBBeanType.class);
        assertEquals(in.value, out.value);
    }

    
    @Path("/")
    @Produces("application/xml")
    @Consumes("application/xml")
    public static class JAXBListResource {
        @POST
        public List<JAXBBean> post(List<JAXBBean> l) {
            return l;            
        }

        @GET
        public Collection<JAXBBean> get() {
            ArrayList<JAXBBean> l = new ArrayList<JAXBBean>();
            l.add(new JAXBBean("one"));
            l.add(new JAXBBean("two"));
            l.add(new JAXBBean("three"));
            return l;
        }
        
        @POST
        @Path("type")
        public List<JAXBBean> postType(Collection<JAXBBeanType> l) {
            List<JAXBBean> beans = new ArrayList<JAXBBean>();
            for (JAXBBeanType t : l)
                beans.add(new JAXBBean(t.value));
            return beans;            
        }        
    }
    
    @Path("/")
    @Produces("application/xml")
    @Consumes("application/xml")
    public static class JAXBArrayResource {
        @POST
        public JAXBBean[] post(JAXBBean[] l) {
            return l;
        }

        @GET
        public JAXBBean[] get() {
            ArrayList<JAXBBean> l = new ArrayList<JAXBBean>();
            l.add(new JAXBBean("one"));
            l.add(new JAXBBean("two"));
            l.add(new JAXBBean("three"));
            return l.toArray(new JAXBBean[l.size()]);
        }

        @POST
        @Path("type")
        public JAXBBean[] postType(JAXBBeanType[] l) {
            List<JAXBBean> beans = new ArrayList<JAXBBean>();
            for (JAXBBeanType t : l)
                beans.add(new JAXBBean(t.value));
            return beans.toArray(new JAXBBean[beans.size()]);
        }
    }

    @Test
    public void testJAXBArrayRepresentation() {
        initiateWebApplication(JAXBArrayResource.class);
        WebResource r = resource("/");

        JAXBBean[] a = r.get(JAXBBean[].class);
        JAXBBean[] b = r.type("application/xml").post(JAXBBean[].class, a);
        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++)
            assertEquals(a[i], b[i]);

        b = r.path("type").type("application/xml").post(JAXBBean[].class, a);
        assertEquals(a.length, b.length);
        for (int i = 0; i < a.length; i++)
            assertEquals(a[i], b[i]);
    }



    @Path("/")
    @Produces("application/foo+xml")
    @Consumes("application/foo+xml")
    public static class JAXBListResourceMediaType extends JAXBListResource {
    }

    @Test
    public void testJAXBListRepresentationMediaType() {
        initiateWebApplication(JAXBListResourceMediaType.class);
        WebResource r = resource("/");


        Collection<JAXBBean> a = r.get(
                new GenericType<Collection<JAXBBean>>(){});
        Collection<JAXBBean> b = r.type("application/foo+xml").post(new GenericType<Collection<JAXBBean>>(){},
                new GenericEntity<Collection<JAXBBean>>(a){});

        assertEquals(a, b);

        b = r.path("type").type("application/foo+xml").post(new GenericType<Collection<JAXBBean>>(){},
                new GenericEntity<Collection<JAXBBean>>(a){});
        assertEquals(a, b);
    }



    @Test
    public void testJAXBListRepresentationError() {
        initiateWebApplication(JAXBListResource.class);
        WebResource r = resource("/", false);

        String xml = "<root><value>foo";
        ClientResponse cr = r.type("application/xml").post(ClientResponse.class, xml);
        assertEquals(400, cr.getStatus());
    }

    @Path("/")
    @Produces("application/fastinfoset")
    @Consumes("application/fastinfoset")
    public static class JAXBListResourceFastInfoset extends JAXBListResource {
    }

    /**
     * TODO, the unmarshalling fails.
     */
    public void _testJAXBListRepresentationFastInfoset() {
        initiateWebApplication(JAXBListResourceFastInfoset.class);
        WebResource r = resource("/");

        Collection<JAXBBean> a = r.get(
                new GenericType<Collection<JAXBBean>>(){});
        Collection<JAXBBean> b = r.post(new GenericType<Collection<JAXBBean>>(){}, 
                new GenericEntity<Collection<JAXBBean>>(a){});
        
        assertEquals(a, b);
        
        b = r.path("type").post(new GenericType<Collection<JAXBBean>>(){},
                new GenericEntity<Collection<JAXBBean>>(a){});
        assertEquals(a, b);
    }

    @Path("/")
    @Produces("application/json")
    @Consumes("application/json")
    public static class JAXBListResourceJSON extends JAXBListResource {
    }


    @Test
    public void testJAXBListRepresentationJSON() throws Exception {
        initiateWebApplication(JAXBListResourceJSON.class);
        WebResource r = resource("/");

        Collection<JAXBBean> a = r.get(
                new GenericType<Collection<JAXBBean>>(){});
        Collection<JAXBBean> b = r.type("application/json").post(new GenericType<Collection<JAXBBean>>(){},
                new GenericEntity<Collection<JAXBBean>>(a){});

        assertEquals(a, b);

        b = r.path("type").type("application/json").post(new GenericType<Collection<JAXBBean>>(){},
                new GenericEntity<Collection<JAXBBean>>(a){});
        assertEquals(a, b);

        // TODO: would be nice to produce/consume a real JSON array like following
        // instead of what we have now:
//        JSONArray a = r.get(JSONArray.class);
//        JSONArray b = new JSONArray().
//                put(new JSONObject().put("value", "one")).
//                put(new JSONObject().put("value", "two")).
//                put(new JSONObject().put("value", "three"));
//        assertEquals(a.toString(), b.toString());
//        JSONArray c = r.post(JSONArray.class, b);
//        assertEquals(a.toString(), c.toString());
    }

    @Path("/")
    @Produces("application/foo+json")
    @Consumes("application/foo+json")
    public static class JAXBListResourceJSONMediaType extends JAXBListResource {
    }

    @Test
    public void testJAXBListRepresentationJSONMediaType() throws Exception {
        initiateWebApplication(JAXBListResourceJSONMediaType.class);
        WebResource r = resource("/");

        Collection<JAXBBean> a = r.get(
                new GenericType<Collection<JAXBBean>>(){});
        Collection<JAXBBean> b = r.type("application/foo+json").post(new GenericType<Collection<JAXBBean>>(){},
                new GenericEntity<Collection<JAXBBean>>(a){});

        assertEquals(a, b);

        b = r.path("type").type("application/foo+json").post(new GenericType<Collection<JAXBBean>>(){},
                new GenericEntity<Collection<JAXBBean>>(a){});
        assertEquals(a, b);

        // TODO: would be nice to produce/consume a real JSON array like following
        // instead of what we have now:
//        JSONArray a = r.get(JSONArray.class);
//        JSONArray b = new JSONArray().
//                put(new JSONObject().put("value", "one")).
//                put(new JSONObject().put("value", "two")).
//                put(new JSONObject().put("value", "three"));
//        assertEquals(a.toString(), b.toString());
//        JSONArray c = r.post(JSONArray.class, b);
//        assertEquals(a.toString(), c.toString());
    }
}
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.entity;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.ws.rest.impl.MultivaluedMapImpl;
import com.sun.ws.rest.impl.provider.entity.AtomEntryProvider;
import com.sun.ws.rest.impl.provider.entity.AtomFeedProvider;
import com.sun.ws.rest.impl.provider.entity.FileProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import javax.activation.DataSource;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBElement;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EntityTypesTest extends AbstractTypeTester {
    
    public EntityTypesTest(String testName) {
        super(testName);
    }
    
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
    
    public void testInputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream("CONTENT".getBytes());
        _test(in, InputStreamResource.class);
    }
    
    @Path("/")
    public static class StringResource extends AResource<String> {}
    
    public void testString() {
        _test("CONTENT", StringResource.class);
    }

    @Path("/")
    public static class DataSourceResource extends AResource<DataSource> {}
    
    public void testDataSource() throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream("CONTENT".getBytes());
        ByteArrayDataSource ds = new ByteArrayDataSource(bais, "text/plain");        
        _test(ds, DataSourceResource.class);
    }
    
    @Path("/")
    public static class ByteArrayResource extends AResource<byte[]> {}
    
    public void testByteArrayRepresentation() {
        _test("CONTENT".getBytes(), ByteArrayResource.class);
    }
    
    @Path("/")
    public static class JAXBBeanResource extends AResource<JAXBBean> {}
    
    public void testJAXBBeanRepresentation() {
        _test(new JAXBBean("CONTENT"), JAXBBeanResource.class);
    }
    
    @Path("/")
    public static class JAXBElementBeanResource extends AResource<JAXBElement<JAXBBeanType>> {}
    
    public void testJAXBElementBeanRepresentation() {
        _test(new JAXBBean("CONTENT"), JAXBElementBeanResource.class);
    }
    
    @Path("/")
    public static class FileResource extends AResource<File> {}
    
    public void testFileRepresentation() throws IOException {
        FileProvider fp = new FileProvider();
        File in = fp.readFrom(File.class, File.class, null, null, null, 
                new ByteArrayInputStream("CONTENT".getBytes()));
        
        _test(in, FileResource.class);
    }
    
    @Path("/")
    public static class MimeMultipartBeanResource extends AResource<MimeMultipart> {}
    
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
    
    @ProduceMime("application/x-www-form-urlencoded")
    @ConsumeMime("application/x-www-form-urlencoded")
    @Path("/")
    public static class FormResource extends AResource<Form> {}
    
    public void testFormRepresentation() {
        Form fp = new Form();
        fp.add("Email", "johndoe@gmail.com");
        fp.add("Passwd", "north 23AZ");
        fp.add("service", "cl");
        fp.add("source", "Gulp-CalGul-1.05");
        
        _test(fp, FormResource.class);
    }
    
    @Path("/")
    public static class JSONObjectResource extends AResource<JSONObject> {}
    
    public void testJSONObjectRepresentation() throws Exception {
        JSONObject object = new JSONObject();
        object.put("userid", 1234).
        put("username", "1234").
        put("email", "a@b").
        put("password", "****");
        
        _test(object, JSONObjectResource.class);
    }

    @Path("/")
    public static class JSONOArrayResource extends AResource<JSONArray> {}
    
    public void testJSONArrayRepresentation() throws Exception {
        JSONArray array = new JSONArray();
        array.put("One").put("Two").put("Three").put(1).put(2.0);
        
        _test(array, JSONOArrayResource.class);
    }
    
    @Path("/")
    public static class FeedResource extends AResource<Feed> {}
    
    public void testFeedRepresentation() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("feed.xml");
        AtomFeedProvider afp = new AtomFeedProvider();
        Feed f = afp.readFrom(Feed.class, Feed.class, null, null, null, in);
        
        _test(f, FeedResource.class);
    }
    
    @Path("/")
    public static class EntryResource extends AResource<Entry> {}
    
    public void testEntryRepresentation() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("entry.xml");        
        AtomEntryProvider afp = new AtomEntryProvider();
        Entry e = afp.readFrom(Entry.class, Entry.class, null, null, null, in);
        
        _test(e, EntryResource.class);
    }
    
    @Path("/")
    public static class ReaderResource extends AResource<Reader> {}
    
    public void testReaderRepresentation() throws Exception {        
        _test(new StringReader("CONTENT"), ReaderResource.class);
    }
    
    private final static String XML_DOCUMENT="<n:x xmlns:n=\"urn:n\"><n:e>CONTNET</n:e></n:x>";
    
    @Path("/")
    public static class StreamSourceResource extends AResource<StreamSource> {}
    
    public void testStreamSourceRepresentation() throws Exception {
        StreamSource ss = new StreamSource(
                new ByteArrayInputStream(XML_DOCUMENT.getBytes()));
        _test(ss, StreamSourceResource.class);
    }
    
    @Path("/")
    public static class SAXSourceResource extends AResource<SAXSource> {}
    
    public void testSAXSourceRepresentation() throws Exception {
        StreamSource ss = new StreamSource(
                new ByteArrayInputStream(XML_DOCUMENT.getBytes()));
        _test(ss, SAXSourceResource.class);
    }
    
    @Path("/")
    public static class DOMSourceResource extends AResource<DOMSource> {}
    
    public void testDOMSourceRepresentation() throws Exception {
        StreamSource ss = new StreamSource(
                new ByteArrayInputStream(XML_DOCUMENT.getBytes()));
        _test(ss, DOMSourceResource.class);
    }
    
    @Path("/")
    @ProduceMime("application/x-www-form-urlencoded")
    @ConsumeMime("application/x-www-form-urlencoded")
    public static class FormMultivaluedMapResource {
        @POST
        public MultivaluedMap<String, String> post(MultivaluedMap<String, String> t) {
            return t;
        }
    }
    
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
    
    public void testStreamingOutputRepresentation() throws Exception {
        initiateWebApplication(StreamingOutputResource.class);
        WebResource r = resource("/");
        assertEquals("CONTENT", r.get(String.class));
    }
}
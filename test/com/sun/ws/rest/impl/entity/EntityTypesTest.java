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
import com.sun.ws.rest.api.representation.FormURLEncodedProperties;
import com.sun.ws.rest.impl.provider.entity.AtomEntryProvider;
import com.sun.ws.rest.impl.provider.entity.AtomFeedProvider;
import com.sun.ws.rest.impl.provider.entity.FileProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.Path;
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
    public static class StringResource extends AResource<String> {}
    
    public void testString() {
        _test(String.class, "CONTENT", StringResource.class);
    }

    @Path("/")
    public static class ByteArrayResource extends AResource<byte[]> {}
    
    public void testByteArrayRepresentation() {
        _test(byte[].class, "CONTENT".getBytes(), ByteArrayResource.class);
    }
    
    @Path("/")
    public static class JAXBBeanResource extends AResource<JAXBBean> {}
    
    public void testJAXBBeanRepresentation() {
        _test(JAXBBean.class, new JAXBBean("CONTENT"), JAXBBeanResource.class);
    }
    
    @Path("/")
    public static class FileResource extends AResource<File> {}
    
    public void testFileRepresentation() throws IOException {
        FileProvider fp = new FileProvider();
        File in = fp.readFrom(File.class, null, null, new ByteArrayInputStream("CONTENT".getBytes()));
        
        _test(File.class, in, FileResource.class);
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
        _test(MimeMultipart.class, mmIn, MimeMultipartBeanResource.class, false);
    }
    
    @Path("/")
    public static class FormResource extends AResource<FormURLEncodedProperties> {}
    
    public void testFormRepresentation() {
        FormURLEncodedProperties fp = new FormURLEncodedProperties();
        fp.put("Email", "johndoe@gmail.com");
        fp.put("Passwd", "north 23AZ");
        fp.put("service", "cl");
        fp.put("source", "Gulp-CalGul-1.05");
        
        _test(FormURLEncodedProperties.class, fp, FormResource.class);
    }
    
    @Path("/")
    public static class JSONObjectResource extends AResource<JSONObject> {}
    
    public void testJSONObjectRepresentation() throws Exception {
        JSONObject object = new JSONObject();
        object.put("userid", 1234).
        put("username", "1234").
        put("email", "a@b").
        put("password", "****");
        
        _test(JSONObject.class, object, JSONObjectResource.class);
    }

    @Path("/")
    public static class JSONOArrayResource extends AResource<JSONArray> {}
    
    public void testJSONArrayRepresentation() throws Exception {
        JSONArray array = new JSONArray();
        array.put("One").put("Two").put("Three").put(1).put(2.0);
        
        _test(JSONArray.class, array, JSONOArrayResource.class);
    }
    
    @Path("/")
    public static class FeedResource extends AResource<Feed> {}
    
    public void testFeedRepresentation() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("feed.xml");
        AtomFeedProvider afp = new AtomFeedProvider();
        Feed f = afp.readFrom(Feed.class, null, null, in);
        
        _test(Feed.class, f, FeedResource.class);
    }
    
    @Path("/")
    public static class EntryResource extends AResource<Entry> {}
    
    public void testEntryRepresentation() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("entry.xml");        
        AtomEntryProvider afp = new AtomEntryProvider();
        Entry e = afp.readFrom(Entry.class, null, null, in);
        
        _test(Entry.class, e, EntryResource.class);
    }
    
}
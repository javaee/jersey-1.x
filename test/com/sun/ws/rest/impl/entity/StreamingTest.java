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

import com.sun.ws.rest.api.representation.FormURLEncodedProperties;
import com.sun.ws.rest.impl.provider.entity.FileProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.mail.internet.MimeMultipart;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class StreamingTest extends AbstractStreamingTester {
    
    public StreamingTest(String testName) {
        super(testName);
    }
    
    public void testString() throws IOException {
        roundTrip(String.class, "THIS IS A TEST");
    }

    public void testByteArrayRepresentation() throws IOException {
        roundTrip(byte[].class, "THIS IS A TEST".getBytes());
    }
    
    public void testJAXBRepresentation() throws Exception {
        JAXBContext context = JAXBContext.newInstance(JAXBBean.class);
        Marshaller marshaller = context.createMarshaller();
        
        JAXBBean in = new JAXBBean("THIS IS A TEST");
        roundTrip(JAXBBean.class, in);
    }    
    
    public void testFileRepresentation() throws Exception {
        FileProvider fp = new FileProvider();
        File in = fp.readFrom(File.class, null, null, new ByteArrayInputStream("THIS IS A TEST".getBytes()));
        roundTrip(File.class, in);
    }
    
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

        byte[] content = writeTo(mmIn);
        mmIn = readFrom(MimeMultipart.class, content);
        roundTrip(MimeMultipart.class, mmIn);
    }
    
    public void testFormURLEncodedRepresentation() throws Exception {
        FormURLEncodedProperties fp = new FormURLEncodedProperties();
        fp.put("Email", "johndoe@gmail.com");
        fp.put("Passwd", "north 23AZ");
        fp.put("service", "cl");
        fp.put("source", "Gulp-CalGul-1.05");
        
        roundTrip(FormURLEncodedProperties.class, fp);
    }
    
    public void testJSONArray() throws Exception {
        JSONArray array = new JSONArray();
        array.put("One").put("Two").put("Three").put(1).put(2.0);
        roundTrip(JSONArray.class, array);
    }
    
    public void testJSONObject() throws Exception {
        JSONObject object = new JSONObject();
        object.put("userid", 1234).
        put("username", "1234").
        put("email", "a@b").
        put("password", "****");
        
        roundTrip(JSONObject.class, object);
    }
}

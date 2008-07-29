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
package com.sun.jersey.impl.methodparams;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.impl.entity.JAXBBean;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FormParamTest extends AbstractResourceTester {

    public FormParamTest(String testName) {
        super(testName);
    }
    
    @Path("/")
    public class FormResourceX {
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public String post(@FormParam("a") String a, @FormParam("b") String b,
                MultivaluedMap<String, String> form) {
            assertEquals(a, form.getFirst("a"));
            assertEquals(b, form.getFirst("b"));
            return a + b;
        }
    }
    
    @Path("/")
    public class FormResourceY {
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public String post(@FormParam("a") String a, @FormParam("b") String b,
                Form form) {
            assertEquals(a, form.getFirst("a"));
            assertEquals(b, form.getFirst("b"));
            return a + b;
        }
    }
    
    public void testFormParamX() {
        initiateWebApplication(FormResourceX.class);
        
        WebResource r = resource("/");
        
        Form form = new Form();
        form.add("a", "foo");
        form.add("b", "bar");        
        
        String s = r.post(String.class, form);
        assertEquals("foobar", s);
    }    
    
    public void testFormParamY() {
        initiateWebApplication(FormResourceY.class);
        
        WebResource r = resource("/");
        
        Form form = new Form();
        form.add("a", "foo");
        form.add("b", "bar");        
        
        String s = r.post(String.class, form);
        assertEquals("foobar", s);
    }    
    
    @Path("/")
    public class MultipartFormResourceX {
        @POST
        @Consumes({"multipart/form-data", MediaType.APPLICATION_FORM_URLENCODED})
        public String post(
                @FormParam("a") String a, 
                @FormParam("b") String b,
                @FormParam("c") JAXBBean c,
                MimeMultipart m) throws Exception {
            assertEquals(3, m.getCount());
            return a + b;
        }
    }
    
    @Path("/")
    public class MultipartFormResourceY {
        @POST
        @Consumes({"multipart/form-data", MediaType.APPLICATION_FORM_URLENCODED})
        public String post(
                @FormParam("a") String a, 
                @FormParam("b") String b,
                @FormParam("c") JAXBBean c,
                Form form) throws Exception {
            assertEquals(a, form.getFirst("a"));
            assertEquals(b, form.getFirst("b"));
            return a + b;
        }
    }
    
    public void testMultipartFormParam() throws Exception {
        initiateWebApplication(MultipartFormResourceX.class);
        
        WebResource r = resource("/");
                
        MimeMultipart form = new MimeMultipart();
        
        InternetHeaders headers = new InternetHeaders();
        headers.addHeader("content-disposition", "form-data; name=\"a\"");
        MimeBodyPart bp = new MimeBodyPart(headers, "foo".getBytes());
        form.addBodyPart(bp);

        headers = new InternetHeaders();
        headers.addHeader("content-disposition", "form-data; name=\"b\"");
        bp = new MimeBodyPart(headers, "bar".getBytes());
        form.addBodyPart(bp);

        headers = new InternetHeaders();
        headers.addHeader("content-disposition", "form-data; name=\"c\"");
        headers.addHeader("Content-type", "application/xml");
        bp = new MimeBodyPart(headers, "<jaxbBean><value>content</value></jaxbBean>".getBytes());
        form.addBodyPart(bp);
        
        String s = r.type("multipart/form-data").post(String.class, form);
        assertEquals("foobar", s);
    }
    
    public void testMultipartFormParamWithForm() {
        initiateWebApplication(MultipartFormResourceY.class);
        
        WebResource r = resource("/");
        
        Form form = new Form();
        form.add("a", "foo");
        form.add("b", "bar");
        form.add("c", "<jaxbBean><value>content</value></jaxbBean>");
        
        String s = r.post(String.class, form);
        assertEquals("foobar", s);
    }
}
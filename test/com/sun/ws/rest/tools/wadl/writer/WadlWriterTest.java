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

package com.sun.ws.rest.tools.wadl.writer;

import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import com.sun.ws.rest.tools.annotation.Method;
import com.sun.ws.rest.tools.annotation.Param;
import com.sun.ws.rest.tools.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import junit.framework.*;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Properties;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class WadlWriterTest extends TestCase {
    
    public WadlWriterTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of writeTo method, of class com.sun.ws.rest.wadl.writer.WadlWriter.
     */
    public void testWriteTo() throws Exception {
        System.out.println("writeTo");
        
        File tmpFile = File.createTempFile("REST", null);
        OutputStream out = new FileOutputStream(tmpFile);
        
        AnnotationProcessorContext context = new AnnotationProcessorContext();
        Resource r1 = new Resource("classname", "/widgets/{widget}");
        context.getResources().add(r1);
        Resource r2 = new Resource("classname", "/widgets");
        context.getResources().add(r2);
        Method m1 = new Method("GET", r1);
        m1.setProduces("image/jpg");
        m1.setOutputEntity(true);
        Method m2 = new Method("GET", r1);
        m2.setProduces("application/widgets+xml");
        m2.setOutputEntity(true);
        QName booleanType = new QName("http://www.w3.org/2001/XMLSchema", "boolean", "xs");
        m2.getParams().add(new Param("id", Param.Style.QUERY, null, null, false));
        m2.getParams().add(new Param("verbose", Param.Style.QUERY, "false", booleanType, false));
        Method m3 = new Method("PUT", r1);
        m3.setProduces("application/widgets+xml");
        m3.setConsumes("application/widgets+xml");
        m3.getParams().add(new Param("data", Param.Style.ENTITY, null, null, false));
        m3.setInputEntity(true);
        m3.setOutputEntity(true);
        Method m4 = new Method("POST", r2);
        m4.setConsumes("application/widgets+xml");
        m4.getParams().add(new Param("data", Param.Style.ENTITY, null, null, false));
        m4.setInputEntity(true);
        
        WadlWriter instance = new WadlWriter(context);
        instance.writeTo(out);
        out.flush();
        out.close();
        
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"%%REPLACE%%");
        val = (String)xp.evaluate("count(/wadl:application/wadl:resources/wadl:resource[@path='/widgets/{widget}'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        val = (String)xp.evaluate("count(/wadl:application/wadl:resources/wadl:resource[@path='/widgets'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        val = (String)xp.evaluate("count(//wadl:resource[@path='/widgets/{widget}']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        val = (String)xp.evaluate("count(//wadl:resource[@path='/widgets']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        val = (String)xp.evaluate("//wadl:param[@name='verbose']/@default", d, XPathConstants.STRING);
        assertEquals(val,"false");
        val = (String)xp.evaluate("//wadl:param[@name='verbose']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:boolean");
    }
    
    private static class NSResolver implements NamespaceContext {
        private String prefix;
        private String nsURI;
        
        public NSResolver(String prefix, String nsURI) {
            this.prefix = prefix;
            this.nsURI = nsURI;
        }
        
        public String getNamespaceURI(String prefix) {
             if (prefix.equals(this.prefix))
                 return this.nsURI;
             else
                 return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespaceURI) {
            if (namespaceURI.equals(this.nsURI))
                return this.prefix;
            else
                return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }

    
    private static void printSource(Source source) {
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            oprops.put(OutputKeys.INDENT, "yes");
            oprops.put(OutputKeys.METHOD, "xml");
            trans.setOutputProperties(oprops);
            trans.transform(source, new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

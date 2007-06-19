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

package com.sun.ws.rest.annotation;

import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.utils.AptInvoker;
import com.sun.ws.rest.utils.BuildException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import junit.framework.*;
import org.w3c.dom.Document;

/**
 *
 * @author Doug Kohlert
 */
public class UriTemplateProcessorTest extends TestCase {
    
    public UriTemplateProcessorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of process method, of class com.sun.ws.rest.annotation.URITemplateProcessor.
     */
    public void testProcess() throws Exception {
        System.out.println("process");
        AptInvoker apt = new AptInvoker();
        File dest = new File("dest");
        dest.mkdir();
        apt.setSourcepath(".");
        apt.setDestdir(dest);
        apt.setSourcedestdir(dest);
        Set<String> files = new HashSet<String>();
        files.add("test/com/sun/ws/rest/annotation/MyResource.java");
        apt.setSourceFiles(files);
        apt.setVerbose(true);
        System.out.println("user.dir: "+ System.getProperty("user.dir"));
        try {
            apt.execute();
        } catch (BuildException e) {
            e.printStackTrace();
            assert(false);
        }
        validateResources(dest);
        validateWADL(dest);
        deleteFile(dest);
    }
    
    private void validateResources(File dest) throws Exception {
        URL[] urls = {fileToURL(dest)};
        URLClassLoader classLoader = URLClassLoader.newInstance(urls);
        Class resClass = classLoader.loadClass("com.sun.ws.rest.annotation.MyResource");
        Class beansClass = classLoader.loadClass("webresources.WebResources");
        Class wadlClass = classLoader.loadClass("com.sun.ws.rest.wadl.resource.WadlResource");
        ResourceConfig resBeans = (ResourceConfig)beansClass.newInstance();
        assertTrue(resBeans.getResourceClasses().size() == 2);
        assertTrue(resBeans.getResourceClasses().contains(resClass));
        assertTrue(resBeans.getResourceClasses().contains(wadlClass));
    }
    
    private void validateWADL(File destDir) throws Exception {
        File wadl = new File(destDir, "com/sun/ws/rest/wadl/resource/application.wadl");
        assertTrue(wadl.exists());

        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(wadl);
        System.out.println("WADL File:");
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertTrue(val.contains("%%REPLACE%%"));
        val = (String)xp.evaluate("count(/wadl:application/wadl:resources/wadl:resource[@path='/widgets'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        val = (String)xp.evaluate("count(//wadl:resource[@path='/widgets']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        val = (String)xp.evaluate("//wadl:param[@name='verbose']/@default", d, XPathConstants.STRING);
        assertEquals(val,"false");
        val = (String)xp.evaluate("//wadl:param[@name='verbose']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:boolean");
    }
    
    /**
     * Returns the directory or JAR file URL corresponding to the specified
     * local file name.
     *
     * @param file the File object
     * @return the resulting directory or JAR file URL, or null if unknown
     */
    public static URL fileToURL(File file) {
        String name;
        try {
            name = file.getCanonicalPath();
        } catch (IOException e) {
            name = file.getAbsolutePath();
        }
        name = name.replace(File.separatorChar, '/');
        if (!name.startsWith("/")) {
            name = "/" + name;
        }

        // If the file does not exist, then assume that it's a directory
        if (!file.isFile()) {
            name = name + "/";
        }
        try {
            return new URL("file", "", name);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("file");
        }
    }
    
    public static boolean deleteFile(File file) {
        if (file.isDirectory())
            for (File sub : file.listFiles())
                if (deleteFile(sub) != true)
                    return false;
        return file.delete();
    }
/*
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }*/
    
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

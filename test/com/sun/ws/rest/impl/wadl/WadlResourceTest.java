/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.wadl;

import com.sun.ws.rest.api.MediaTypes;
import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.impl.client.ResourceProxy;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.PathParam;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author mh124079
 */
public class WadlResourceTest extends AbstractResourceTester {
    
    public WadlResourceTest(String testName) {
        super(testName);
    }
    
    @Path("foo")
    public static class ExtraResource {
        @GET
        @ProduceMime("application/xml")
        public String getRep() {
            return null;
        }
    }

    @Path("widgets")
    public static class WidgetsResource {

        @GET
        @ProduceMime({"application/xml", "application/json"})
        public String getWidgets() {
            return null;
        }

        @POST
        @ConsumeMime({"application/xml"})
        @ProduceMime({"application/xml", "application/json"})
        public String createWidget(String bar) {
            return bar;
        }

        @PUT
        @Path("{id}")
        @ConsumeMime("application/xml")
        public void updateWidget(String bar, @PathParam("id")int id) {
        }

        @GET
        @Path("{id}")
        @ProduceMime({"application/xml", "application/json"})
        public String getWidget(@PathParam("id")int id) {
            return null;
        }

        @DELETE
        @Path("{id}")
        public void deleteWidget(@PathParam("id")int id) {
        }

        @Path("{id}/verbose")
        public Object getVerbose(@PathParam("id")int id) {
            return new ExtraResource();
        }
    }
    
    /**
     * Test WADL generation
     */
    public void testGetWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        ResourceProxy r = resourceProxy("/application.wadl");
        
        File tmpFile = r.get(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        // check base URI
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"/base/");
        // check total number of resources is 4
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"4");
        // check only once resource with for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for {id}/verbose
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}/verbose'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 3 methods for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check 2 methods for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check type of {id} is int
        val = (String)xp.evaluate("//wadl:resource[@path='{id}']/wadl:param[@name='id']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:int");
        // check number of output representations is two
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='GET']/wadl:response/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check number of output representations is one
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='POST']/wadl:request/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"1");
    }

    public void testGetResourceWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        ResourceProxy r = resourceProxy("/widgets");
        
        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).get(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));

        // check base URI
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"/base/");
        // check total number of resources is 3 (no ExtraResource details included)
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check only once resource with for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for {id}/verbose
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}/verbose'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 3 methods for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check 2 methods for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check type of {id} is int
        val = (String)xp.evaluate("//wadl:resource[@path='{id}']/wadl:param[@name='id']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:int");
        // check number of output representations is two
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='GET']/wadl:response/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check number of output representations is one
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='POST']/wadl:request/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"1");

        // test ExtraResource
        r = resourceProxy("/foo");
        
        tmpFile = r.accept(MediaTypes.WADL).get(File.class);
        b = bf.newDocumentBuilder();
        d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        // check base URI
        val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"/base/");
        // check total number of resources is 1 (no ExtraResource details included)
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with path foo
        val = (String)xp.evaluate("count(//wadl:resource[@path='foo'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 1 methods for foo
        val = (String)xp.evaluate("count(//wadl:resource[@path='foo']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"1");
    }
    
    public void testOptionsResourceWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        ResourceProxy r = resourceProxy("/widgets");
        
        // test WidgetsResource
        File tmpFile = r.options(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));

        // check base URI
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"/base/");
        // check total number of resources is 3 (no ExtraResource details included)
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check only once resource with for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for {id}/verbose
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}/verbose'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 3 methods for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check 2 methods for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check type of {id} is int
        val = (String)xp.evaluate("//wadl:resource[@path='{id}']/wadl:param[@name='id']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:int");
        // check number of output representations is two
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='GET']/wadl:response/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check number of output representations is one
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='POST']/wadl:request/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"1");

        // test ExtraResource
        r = resourceProxy("/foo");
        
        tmpFile = r.options(File.class);
        b = bf.newDocumentBuilder();
        d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        // check base URI
        val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"/base/");
        // check total number of resources is 1 (no ExtraResource details included)
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with path foo
        val = (String)xp.evaluate("count(//wadl:resource[@path='foo'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 1 methods for foo
        val = (String)xp.evaluate("count(//wadl:resource[@path='foo']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"1");
    }
    
    public void testGetLocatorWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        ResourceProxy r = resourceProxy("/widgets/3/verbose");
        
        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).get(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        // check base URI
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"/base/");
        // check total number of resources is 1 (no ExtraResource details included)
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with path 
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets/3/verbose'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 1 methods
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets/3/verbose']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"1");
    }
    
    public void testGetSubResourceWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        ResourceProxy r = resourceProxy("/widgets/3");
        
        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).get(File.class);
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
        assertEquals(val,"/base/");
        // check total number of resources is 1
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only one resource with for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets/3'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 3 methods
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets/3']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
    }
    
    @Path("root")
    public static class RootResource {
        @Path("loc")
        public Object getSub() {
            return new SubResource();
        }
    }
    
    public static class SubResource {
        @Path("loc")
        public Object getSub() {
            return new SubResource();
        }
        
        @GET
        @ProduceMime("text/plain")
        public String hello() {
            return "Hello World !";
        }
        
        @GET
        @Path("sub")
        @ProduceMime("text/plain")
        public String helloSub() {
            return "Hello World !";
        }
    }
    
    public void testRecursive() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(RootResource.class);
        ResourceProxy r = resourceProxy("/root/loc");
        
        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).get(File.class);
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
        assertEquals(val,"/base/");
        // check only one resource with for 'root/loc'
        val = (String)xp.evaluate("count(//wadl:resource[@path='root/loc'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        
        r = resourceProxy("/root/loc/loc");
        
        // test WidgetsResource
        tmpFile = r.accept(MediaTypes.WADL).get(File.class);
        bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        b = bf.newDocumentBuilder();
        d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"/base/");
        // check only one resource with for 'root/loc'
        val = (String)xp.evaluate("count(//wadl:resource[@path='root/loc/loc'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        
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
            System.out.println("---------------------");
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

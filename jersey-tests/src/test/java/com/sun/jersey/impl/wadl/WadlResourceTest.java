/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.impl.wadl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.impl.entity.JAXBBean;
import com.sun.jersey.server.wadl.WadlApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
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
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

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
        @Produces("application/xml")
        public String getRep() {
            return null;
        }
    }

    @Path("widgets")
    public static class WidgetsResource {

        @GET
        @Produces({"application/xml", "application/json"})
        public String getWidgets() {
            return null;
        }

        @POST
        @Consumes({"application/xml"})
        @Produces({"application/xml", "application/json"})
        public String createWidget(String bar) {
            return bar;
        }

        @PUT
        @Path("{id}")
        @Consumes("application/xml")
        public void updateWidget(String bar, @PathParam("id")int id) {
        }

        @GET
        @Path("{id}")
        @Produces({"application/xml", "application/json"})
        public String getWidget(@PathParam("id")int id) {
            return null;
        }

        @DELETE
        @Path("{id}")
        public void deleteWidget(@PathParam("id")int id) {
        }

        @Path("{id}/verbose")
        public ExtraResource getVerbose(@PathParam("id")int id) {
            return new ExtraResource();
        }
    }

    public void testDisableWadl() {
        ResourceConfig rc = new DefaultResourceConfig(WidgetsResource.class, ExtraResource.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, true);
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
    }

    public void testEnableWadl() {
        ResourceConfig rc = new DefaultResourceConfig(WidgetsResource.class, ExtraResource.class);
        rc.getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, false);
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
    }

    /**
     * Test WADL generation
     */
    public void testGetWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        WebResource r = resource("/application.wadl");

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
        assertEquals(val,BASE_URI.toString());
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
        // check type of {id}/verbose is int
        val = (String)xp.evaluate("//wadl:resource[@path='{id}/verbose']/wadl:param[@name='id']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:int");
    }

    public void testOptionsResourceWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        WebResource r = resource("/widgets");

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
        assertEquals(val,BASE_URI.toString());
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
        r = resource("/foo");

        tmpFile = r.options(File.class);
        b = bf.newDocumentBuilder();
        d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        // check base URI
        val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
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

    public void testOptionsLocatorWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        WebResource r = resource("/widgets/3/verbose");

        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).options(File.class);
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
        assertEquals(val,BASE_URI.toString());
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

    public void testOptionsSubResourceWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        WebResource r = resource("/widgets/3");

        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).options(File.class);
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
        assertEquals(val,BASE_URI.toString());
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

        @Path("switch")
        @POST
        public void switchMethod(@Context WadlApplicationContext wadlApplicationContext) {
            wadlApplicationContext.setWadlGenerationEnabled(!wadlApplicationContext.isWadlGenerationEnabled());

        }
    }

    public static class SubResource {
        @Path("loc")
        public Object getSub() {
            return new SubResource();
        }

        @GET
        @Produces("text/plain")
        public String hello() {
            return "Hello World !";
        }

        @GET
        @Path("sub")
        @Produces("text/plain")
        public String helloSub() {
            return "Hello World !";
        }
    }

    public void testRecursive() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(RootResource.class);
        WebResource r = resource("/root/loc");

        // test WidgetsResource
        File tmpFile = r.accept(MediaTypes.WADL).options(File.class);
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
        assertEquals(val,BASE_URI.toString());
        // check only one resource with for 'root/loc'
        val = (String)xp.evaluate("count(//wadl:resource[@path='root/loc'])", d, XPathConstants.STRING);
        assertEquals(val,"1");

        r = resource("/root/loc/loc");

        // test WidgetsResource
        tmpFile = r.accept(MediaTypes.WADL).options(File.class);
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
        assertEquals(val,BASE_URI.toString());
        // check only one resource with for 'root/loc'
        val = (String)xp.evaluate("count(//wadl:resource[@path='root/loc/loc'])", d, XPathConstants.STRING);
        assertEquals(val,"1");

    }

    @Path("root1")
    public static class RootResource1 {
        @Path("loc")
        public SubResource getSub() {
            return new SubResource();
        }
    }

    @Path("root2")
    public static class RootResource2 {
        @Path("loc")
        public SubResource getSub() {
            return new SubResource();
        }
    }

    public void testRecursive2() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(RootResource1.class, RootResource2.class);
        WebResource r = resource("/application.wadl");

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
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,BASE_URI.toString());
        // check only one resource with for 'root/loc'
        val = (String)xp.evaluate("count(//wadl:resource[@path='loc'])", d, XPathConstants.STRING);
        assertEquals("4", val);
        // check for method with id of hello
        val = (String)xp.evaluate("count(//wadl:resource[@path='loc']/wadl:method[@id='hello'])", d, XPathConstants.STRING);
        assertEquals("2", val);
    }


    @Path("form")
    public static class FormResource {

        @POST
        @Consumes( "application/x-www-form-urlencoded" )
        public void post(
                @FormParam( "a" ) String a,
                @FormParam( "b" ) String b,
                @FormParam( "c" ) JAXBBean c,
                @FormParam( "c" ) FormDataContentDisposition cdc,
                Form form ) {
        }

    }

    public void testFormParam() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(FormResource.class);
        WebResource r = resource("/application.wadl");

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

        final String requestPath = "//wadl:resource[@path='form']/wadl:method[@name='POST']/wadl:request";
        final String representationPath = requestPath + "/wadl:representation";

        // check number of request params is zero
        int count = ( (Double)xp.evaluate("count(" + requestPath + "/wadl:param)", d, XPathConstants.NUMBER) ).intValue();
        assertEquals( 0, count );

        // check number of request representations is one
        count = ( (Double)xp.evaluate("count(" + representationPath + ")", d, XPathConstants.NUMBER) ).intValue();
        assertEquals( 1, count );

        // check number of request representation params is three
        count = ( (Double)xp.evaluate("count(" + representationPath + "/wadl:param)", d, XPathConstants.NUMBER) ).intValue();
        assertEquals( 3, count );

        // check the style of the request representation param is 'query'
        String val = (String)xp.evaluate( representationPath + "/wadl:param[@name='a']/@style", d, XPathConstants.STRING);
        assertEquals( "query", val );
        val = (String)xp.evaluate( representationPath + "/wadl:param[@name='b']/@style", d, XPathConstants.STRING);
        assertEquals( "query", val );

    }


    @Path("fieldParam/{pp}")
    public static class FieldParamResource {

        @HeaderParam("hp") String hp;
        @MatrixParam("mp") String mp;
        @PathParam("pp") String pp;
        @QueryParam("q") String q;

        @GET
        @Produces("text/plain" )
        public String get() {
            return pp;
        }

    }

    public void testFieldParam() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        _testFieldAndSetterParam(FieldParamResource.class, "fieldParam");
    }

    @Path("setterParam/{pp}")
    public static class SetterParamResource {

        @HeaderParam("hp")
        public void setHp(String hp) {};

        @MatrixParam("mp")
        public void setMp(String mp) {};

        @PathParam("pp")
        public void setPP(String pp) {};

        @QueryParam("q")
        public void setQ(String q) {};

        @GET
        @Produces("text/plain" )
        public String get() {
            return "nonsense";
        }

    }

    public void testSetterParam() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        _testFieldAndSetterParam(SetterParamResource.class, "setterParam");
    }

    public void testEnableDisableRuntime() {
        initiateWebApplication(RootResource.class);
        WebResource r = resource("/", false);
        r.addFilter(new LoggingFilter());

        ClientResponse response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);

        response = r.path("root").options(ClientResponse.class);
        assertTrue(response.getStatus() == 200);

        r.path("root/switch").post();

        response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 404);

        response = r.path("root").options(ClientResponse.class);
        assertTrue(response.getStatus() == 204);

        r.path("root/switch").post();

        response = r.path("application.wadl").get(ClientResponse.class);
        assertTrue(response.getStatus() == 200);

        response = r.path("root").options(ClientResponse.class);
        assertTrue(response.getStatus() == 200);
    }

    private void _testFieldAndSetterParam(Class resourceClass, String path) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(resourceClass);
        WebResource r = resource("/application.wadl");

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

        final String resourcePath = String.format("//wadl:resource[@path='%s/{pp}']", path);
        final String methodPath = resourcePath + "/wadl:method[@name='GET']";

        // check number of resource methods is one
        int methodCount = ( (Double)xp.evaluate("count(" + methodPath + ")", d, XPathConstants.NUMBER) ).intValue();
        assertEquals(1, methodCount );

        Map<String, String> paramStyles = new HashMap<String, String>();

        paramStyles.put("hp", "header");
        paramStyles.put("mp", "matrix");
        paramStyles.put("pp", "template");
        paramStyles.put("q", "query");

        for(Entry<String, String> param : paramStyles.entrySet()) {

            String pName = param.getKey();
            String pStyle = param.getValue();

            String paramXPath = String.format("%s/wadl:param[@name='%s']", resourcePath, pName);

            // check number of params is one
            int pc = ( (Double)xp.evaluate("count(" + paramXPath + ")", d, XPathConstants.NUMBER) ).intValue();
            assertEquals(1, pc );

            // check the style of the param
            String style = (String)xp.evaluate(paramXPath + "/@style", d, XPathConstants.STRING);
            assertEquals(pStyle, style );
        }
    }

    private static class NSResolver implements NamespaceContext {
        private String prefix;
        private String nsURI;

        public NSResolver(String prefix, String nsURI) {
            this.prefix = prefix;
            this.nsURI = nsURI;
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix.equals(this.prefix))
                return this.nsURI;
            else
                return XMLConstants.NULL_NS_URI;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            if (namespaceURI.equals(this.nsURI))
                return this.prefix;
            else
                return null;
        }

        @Override
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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.wadl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.impl.wadl.testdata.schema.JResponseSchemaResource;
import com.sun.jersey.impl.wadl.testdata.schema.MultipleContentTypesResource;
import com.sun.jersey.impl.wadl.testdata.schema.RequestMessage;
import com.sun.jersey.impl.wadl.testdata.schema.ResponseMessage;
import com.sun.jersey.impl.wadl.testdata.schema.SeeAlsoSchemaResource;
import com.sun.jersey.impl.wadl.testdata.schema.SimpleSchemaResource;
import com.sun.jersey.impl.wadl.testdata.schema.TwoNamespacesSchemaResource;
import com.sun.jersey.impl.wadl.testdata.schema.different.ResponseMessageDifferentNamespace;
import com.sun.jersey.server.wadl.generators.WadlGeneratorJAXBGrammarGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Grammars;
import com.sun.research.ws.wadl.Include;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.util.List;

/**
 * Verify the generation of JAXB content model from java beans
 * 
 * TODO:
 * - List<X>, generate a wrapper type with no namespace it seems
 * - What of other generic subtypes?
 * - Simple types such as string? Need a list of likely types
 * - .wadl options on resource - schema import location is going to be wrong
 * DONE
 * - Simple reference
 * - Hidden subtype
 * - Multiple namespace / schemas with cross reference to check that 
 *    imports are written correctly.
 * - JResponse
 * 
 * @author Gerard Davison
 */
public class WadlJAXBContentModelTest extends AbstractResourceTester {

    public WadlJAXBContentModelTest(String testName) {
        super(testName);
    }

    
    public static class SchemaWadlGeneratorConfig extends WadlGeneratorConfig {

        @Override 
        public List<WadlGeneratorDescription> configure() {
            return generator( WadlGeneratorJAXBGrammarGenerator.class ).descriptions();
        }
    }
    
    
    //

    /**
     * Just a simple case with directly referenced types
     */
    public void testReallySimpleSchema() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(SimpleSchemaResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        
        // Right let check that we have a reference to the XSD, so lets cheat
        // and instantiate the Application object.
        
        Application a = (Application)JAXBContext.newInstance(Application.class)
                .createUnmarshaller()
                .unmarshal(cr.getEntityInputStream());
        
        Grammars g = a.getGrammars();
        assertNotNull("Should have a grammar defined", g);
        
        List<Include> includes = g.getInclude();
        assertEquals(1,includes.size());
        
        String href = includes.get(0).getHref();
        assertEquals("application.wadl/xsd0.xsd", href);
        
        // Okay lets see if we can find the right references
        //
        
        Resource resource = a.getResources().get(0).getResource().get(0);
        
        Method get = (Method)resource.getMethodOrResource().get(0);
        QName responseType  = get.getResponse().get(0).getRepresentation().get(0).getElement();
        assertEquals(ResponseMessage.name, responseType);
        
        Method put = (Method)resource.getMethodOrResource().get(1);
        QName requestType  = put.getRequest().getRepresentation().get(0).getElement(); 
        responseType  = put.getResponse().get(0).getRepresentation().get(0).getElement();
        assertEquals(ResponseMessage.name, responseType);
        assertEquals(RequestMessage.name, requestType);
        
        
        
        
        // Just for a quick look
        cr = r.get(ClientResponse.class);        
        String wadlAsString = cr.getEntity(String.class); 
        
        // Okay lets see if we can request the resource
        //
        
        
        WebResource schema = resource(href);
        String schemaAsString = schema.get(String.class);
        cr = schema.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document dom = dbf.newDocumentBuilder().parse(
                schema.get(InputStream.class));
        final Element documentElement = dom.getDocumentElement();
        
        assertEquals(
            "urn:message",
            documentElement.getAttributes().getNamedItem("targetNamespace").getNodeValue());
        
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("xs", "http://www.w3.org/2001/XMLSchema"));
 
        // Expecting two
        String val = (String)xp.evaluate("count(/xs:schema/xs:element)", dom, XPathConstants.STRING);
        assertEquals("Should have an element for each type", "2", val);
    }

    /**
     * Just a simple case with directly referenced types
     */
    public void testMultipleContentTypesSchema() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(MultipleContentTypesResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        
        // Right let check that we have a reference to the XSD, so lets cheat
        // and instantiate the Application object.
        
        Application a = (Application)JAXBContext.newInstance(Application.class)
                .createUnmarshaller()
                .unmarshal(cr.getEntityInputStream());
        
        Grammars g = a.getGrammars();
        assertNotNull("Should have a grammar defined", g);
        
        List<Include> includes = g.getInclude();
        assertEquals(1,includes.size());
        
        String href = includes.get(0).getHref();
        assertEquals("application.wadl/xsd0.xsd", href);
        
        // Okay lets see if we can find the right references
        //
        
        Resource resource = a.getResources().get(0).getResource().get(0);
        
        Method post = (Method)resource.getMethodOrResource().get(0);

        List<Representation> requestRepresentation = post.getRequest().getRepresentation();
        assertEquals(2, requestRepresentation.size());
        for (int counter = 0; counter < requestRepresentation.size(); counter++) {
            QName requestType  = requestRepresentation.get(counter).getElement();
            assertEquals("Reprensentation doesn't match" + counter,RequestMessage.name, requestType);
        }
        
        
        List<Representation> responseRepresentation = post.getResponse().get(0).getRepresentation();
        for (int counter = 0; counter < responseRepresentation.size(); counter++) {
            QName responseType  = responseRepresentation.get(counter).getElement();
            assertEquals("Reprensentation doesn't match" + counter,ResponseMessage.name, responseType);
        }
        
        
    }
    
    
    /**
     * Just a simple case with directly referenced types
     */
    public void testOptionsOnResource() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(SimpleSchemaResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/root/sub", false);

        ClientResponse cr = r.options(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        
        // Right let check that we have a reference to the XSD, so lets cheat
        // and instantiate the Application object.
        
        Application a = (Application)JAXBContext.newInstance(Application.class)
                .createUnmarshaller()
                .unmarshal(cr.getEntityInputStream());
        
        Grammars g = a.getGrammars();
        assertNotNull("Should have a grammar defined", g);
        
        List<Include> includes = g.getInclude();
        assertEquals(1,includes.size());
        
        String href = includes.get(0).getHref();
        assertEquals("test:/base/application.wadl/xsd0.xsd", href);
//        assertEquals("../../application.wadl/xsd0.xsd", href);
        
        // Check to see that the types have been attached
        //
        
        Resource resource = a.getResources().get(0).getResource().get(0);
        
        Method post = (Method)resource.getMethodOrResource().get(0);
        QName requestType  = post.getRequest().getRepresentation().get(0).getElement(); 
        QName responseType  = post.getResponse().get(0).getRepresentation().get(0).getElement();
        assertEquals(ResponseMessage.name, responseType);
        assertEquals(RequestMessage.name, requestType);
        
        
    }
    
    
    /**
     * Just a simple case with a hidden subtype
     */
    public void testSeeAlsoSchema() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(SeeAlsoSchemaResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        
        // Right let check that we have a reference to the XSD, so lets cheat
        // and instantiate the Application object.
        
        Application a = (Application)JAXBContext.newInstance(Application.class)
                .createUnmarshaller()
                .unmarshal(cr.getEntityInputStream());
        
        Grammars g = a.getGrammars();
        assertNotNull("Should have a grammar defined", g);
        
        List<Include> includes = g.getInclude();
        assertEquals(1,includes.size());
        
        String href = includes.get(0).getHref();
        assertEquals("application.wadl/xsd0.xsd", href);
        
        
        // Okay lets see if we can request the resource
        //
        
        WebResource schema = resource(href);
        String schemaAsString = schema.get(String.class);
        cr = schema.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document dom = dbf.newDocumentBuilder().parse(
                schema.get(InputStream.class));
        final Element documentElement = dom.getDocumentElement();
        
        assertEquals(
            "urn:message",
            documentElement.getAttributes().getNamedItem("targetNamespace").getNodeValue());
        
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("xs", "http://www.w3.org/2001/XMLSchema"));
 
        // Expecting two
        String val = (String)xp.evaluate("count(/xs:schema/xs:element)", dom, XPathConstants.STRING);
        assertEquals("Should have an element for each type", "2", val);
        
        // Check we have a reference to the subtype
        val = (String)xp.evaluate("count(/xs:schema/xs:element[@name='responseMessageSubtype'])", dom, XPathConstants.STRING);
        assertEquals("Should have an element for each type", "1", val);        
    }
    
    

    
    /**
     * Just a simple case with two different schemas, one schema references
     * the other so we can verify the import between the two
     */
    public void testReallyTwoNamespaces() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(TwoNamespacesSchemaResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        
        // Right let check that we have a reference to the XSD, so lets cheat
        // and instantiate the Application object.
        
        Application a = (Application)JAXBContext.newInstance(Application.class)
                .createUnmarshaller()
                .unmarshal(cr.getEntityInputStream());
        
        Grammars g = a.getGrammars();
        assertNotNull("Should have a grammar defined", g);
        
        List<Include> includes = g.getInclude();
        assertEquals("Should have two",2,includes.size());
        
        // Imports get added in revserve order, odd.
        String href1 = includes.get(0).getHref();
        assertEquals("application.wadl/xsd1.xsd", href1);

        String href2 = includes.get(1).getHref();
        assertEquals("application.wadl/xsd0.xsd", href2);

        // Okay lets see if we can find the right references
        //
        
        Resource resource = a.getResources().get(0).getResource().get(0);
        Method put = (Method)resource.getMethodOrResource().get(0);
        QName requestType  = put.getRequest().getRepresentation().get(0).getElement(); 
        QName responseType  = put.getResponse().get(0).getRepresentation().get(0).getElement();
        assertEquals(ResponseMessageDifferentNamespace.name, responseType);
        assertEquals(RequestMessage.name, requestType);
        
        
        // Just for a quick look
        cr = r.get(ClientResponse.class);        
        String wadlAsString = cr.getEntity(String.class); 
        
        // Okay lets see if we can request the resource
        //
        
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        
        String hrefs[] = new String[] { href1, href2 };
        String expectedNamespaces[] = new String[] { "urn:message", "urn:message2"};
        String expectedElements[] = new String[] { "2", "1"};
        String checkForImport[] = new String[] { null, "xsd1.xsd"};

        for(int counter=0; counter < hrefs.length; counter++)
        {
            WebResource schema = resource(hrefs[counter]);
            String schemaAsString = schema.get(String.class);
            cr = schema.get(ClientResponse.class);
            assertEquals(200, cr.getStatus());

            Document dom = dbf.newDocumentBuilder().parse(
                    schema.get(InputStream.class));
            final Element documentElement = dom.getDocumentElement();

            assertEquals(
                expectedNamespaces[counter],
                documentElement.getAttributes().getNamedItem("targetNamespace").getNodeValue());

            XPath xp = XPathFactory.newInstance().newXPath();
            xp.setNamespaceContext(new NSResolver("xs", "http://www.w3.org/2001/XMLSchema"));

            // Expecting two in one, one in the other
            String val = (String)xp.evaluate("count(/xs:schema/xs:element)", dom, XPathConstants.STRING);
            assertEquals("One should have two, the other should have the subclass", expectedElements[counter], val);
            
            // Check for a clean cross import
            if(checkForImport[counter]!=null)
            {
                val = (String)xp.evaluate("/xs:schema/xs:import/@schemaLocation", dom, XPathConstants.STRING);
                assertEquals("Should reference the other file", checkForImport[counter], val);                
            }
        }
        
        // <xs:import namespace="urn:message" schemaLocation="xsd1.xsd"/>
    }
    
    

    /**
     * A test case with a JReponse generic parameter return type.
     */
    public void testJResponseSchema() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(JResponseSchemaResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());
        
        // Right let check that we have a reference to the XSD, so lets cheat
        // and instantiate the Application object.
        Application a = (Application)JAXBContext.newInstance(Application.class)
                .createUnmarshaller()
                .unmarshal(cr.getEntityInputStream());
        
        
        // Verify that the QName type is set
        
        Resource resource = a.getResources().get(0).getResource().get(0);
        Method put = (Method)resource.getMethodOrResource().get(0);
        QName responseType  = put.getResponse().get(0).getRepresentation().get(0).getElement();
        assertEquals(ResponseMessage.name, responseType);
        
        
        // Check we have grammars present
        //
        
        Grammars g = a.getGrammars();
        assertNotNull("Should have a grammar defined", g);
        
        List<Include> includes = g.getInclude();
        assertEquals(1,includes.size());
        
        String href = includes.get(0).getHref();
        assertEquals("application.wadl/xsd0.xsd", href);
        
        
        // Okay lets see if we can request the resource
        //
        
        WebResource schema = resource(href);
        String schemaAsString = schema.get(String.class);
        cr = schema.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document dom = dbf.newDocumentBuilder().parse(
                schema.get(InputStream.class));
        final Element documentElement = dom.getDocumentElement();
        
        assertEquals(
            "urn:message",
            documentElement.getAttributes().getNamedItem("targetNamespace").getNodeValue());
        
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("xs", "http://www.w3.org/2001/XMLSchema"));
 
        // Expecting two
        String val = (String)xp.evaluate("count(/xs:schema/xs:element)", dom, XPathConstants.STRING);
        assertEquals("Should just have the single directly reference element", "1", val);
    }
}

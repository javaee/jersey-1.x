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
package com.sun.jersey.impl.wadl;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorDescription;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.impl.entity.EmptyJSONRequestWthJAXBTest;
import com.sun.jersey.impl.entity.JAXBBean;
import com.sun.jersey.impl.entity.JAXBBeanType;
import com.sun.jersey.impl.wadl.testdata.schema.MultipleContentTypesResource;
import com.sun.jersey.impl.wadl.testdata.schema.RequestMessage;
import com.sun.jersey.impl.wadl.testdata.schema.ResponseMessage;
import com.sun.jersey.wadl.generators.json.WadlGeneratorJSONGrammarGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Grammars;
import com.sun.research.ws.wadl.Include;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ext.ContextResolver;
import javax.xml.bind.JAXBContext;

/**
 * Verify the generation of JSON content model from java beans
 * 
 * TODO:
 * - List<X>, generate a wrapper type with no namespace it seems
 * 
 * @author Gerard Davison
 */
public class WadlJSONContentModelTest extends AbstractResourceTester {

    
    public WadlJSONContentModelTest(String testName) {
        super(testName);
    }

    public static class SchemaWadlGeneratorConfig extends WadlGeneratorConfig {

        @Override 
        public List<WadlGeneratorDescription> configure() {
            return generator( WadlGeneratorJSONGrammarGenerator.class ).descriptions();
        }
    }
    
    public static class NaturalCR implements ContextResolver<JAXBContext> {
        
        
        private final JAXBContext context;

        private final Class[] classes = {RequestMessage.class, ResponseMessage.class};

        private final Set<Class> types = new HashSet(Arrays.asList(classes));

        public NaturalCR() {
            try {
                context = configure(classes);
            } catch (JAXBException ex) {
                throw new RuntimeException(ex);
            }
        }

        public JAXBContext getContext(Class<?> objectType) {
            return (types.contains(objectType)) ? context : null;
        }
        
        protected JAXBContext configure(Class[] classes) throws JAXBException {
            return new JSONJAXBContext(JSONConfiguration.natural().build(), classes);
        }
    }
    
    
    /**
     * Just a simple case with directly referenced types
     */
    public void testMultipleContentTypesSchema() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(NaturalCR.class, MultipleContentTypesResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        validateWadl("", cr, r, true);
    }

    /**
     * Like the above but using the POJO mapping rather than the 
     * JAX-B mapping 
     */
    public void testMultipleContentTypesSchemaPOJOMapping() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(MultipleContentTypesResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
        
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.get(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        String asString = r.get(String.class);
        
        validateWadl("", cr, r, true);
    }

    
    /**
     * Just a simple case with directly referenced types as JSON
     */
    public void testMultipleContentTypesSchemaAsJson() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(NaturalCR.class, MultipleContentTypesResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        ClientResponse cr = r.accept(MediaTypes.WADL_JSON).get(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        String wadlAsString = r.accept(MediaTypes.WADL_JSON).get(String.class);
        
        // I was seeing a null pointer desrializing the WADL back into
        // a JAX-B class, so just for the moment use simple contains 
        // to validate that what we are expecting is appearing in the 
        // WADL
        //validateWadl("", cr, r);
        
        
        assertTrue(wadlAsString.contains("\"@describedby\":\"application.wadl/responseMessage\""));
        assertTrue(wadlAsString.contains("\"@describedby\":\"application.wadl/requestMessage\""));
        
        assertTrue(wadlAsString.contains("\"@element\":\"responseMessage\""));
        assertTrue(wadlAsString.contains("\"@element\":\"requestMessage\""));
        
    }
 
    /**
     * Check that even though we have POJO mapping enabled that the WADL
     * is rendered correctly into JSON using the JAX-B Mapped notation,
     * verifies JERSEY-1593
     */
    public void testMultipleContentTypesSchemaAsJsonPojoMapping() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(NaturalCR.class, MultipleContentTypesResource.class);
        rc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,true);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/application.wadl", false);

        String wadlAsString = r.accept(MediaTypes.WADL_JSON).get(String.class);

        // Just a couple of quick check to make sure we are not seeing the 
        // POJO mappings
        assertTrue(!wadlAsString.startsWith("{\"doc\":[{\"content\":null"));
        assertTrue(!wadlAsString.contains("null"));
        
    }
    
    /**
     * A more complex case using options on a resource
     */
    public void testMultipleContentTypesSchemaFromResource() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(NaturalCR.class, MultipleContentTypesResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/root", false);

        ClientResponse cr = r.options(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        String wadlAsString = r.options(String.class);
        
        validateWadl("test:/base/", cr, r, true);
    }

    /**
     * Just a simple case with directly referenced types as JSON
     */
    public void testMultipleContentTypesSchemaFromResourceAsJson() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(NaturalCR.class, MultipleContentTypesResource.class);
        rc.getProperties().put(ResourceConfig.PROPERTY_WADL_GENERATOR_CONFIG, new SchemaWadlGeneratorConfig());
        initiateWebApplication(rc);

        WebResource r = resource("/root", false);

        ClientResponse cr = r.accept(MediaTypes.WADL_JSON).options(ClientResponse.class);
        assertEquals(200, cr.getStatus());

        String wadlAsString = r.accept(MediaTypes.WADL_JSON).options(String.class);

//        Application wadlAsApp = r.accept(MediaTypes.WADL_JSON).options(Application.class);
        
        // I was seeing a null pointer desrializing the WADL back into
        // a JAX-B class, so just for the moment use simple contains 
        // to validate that what we are expecting is appearing in the 
        // WADL
        //validateWadl("", cr, r);
        
        
        assertTrue(wadlAsString.contains("\"@describedby\":\"test:/base/application.wadl/responseMessage\""));
        assertTrue(wadlAsString.contains("\"@describedby\":\"test:/base/application.wadl/requestMessage\""));
        
        assertTrue(wadlAsString.contains("\"@element\":\"responseMessage\""));
        assertTrue(wadlAsString.contains("\"@element\":\"requestMessage\""));
        
    }

    private void validateWadl(
            String uriPrefix, 
            ClientResponse cr, 
            WebResource r,
            boolean hasXml) throws UniformInterfaceException, JAXBException, ClientHandlerException {
        // Right let check that we have a reference to the XSD, so lets cheat
        // and instantiate the Application object.
        
        Application a = cr.getEntity(Application.class);
//                (Application)JAXBContext.newInstance(Application.class)
//                .createUnmarshaller()
//                .unmarshal(cr.getEntityInputStream());
        
        
        Grammars g = a.getGrammars();
        assertNotNull("Should have a grammar defined", g);
        
        // Uses URI references rather than grammar includes,
        // so only expect xml schema here
        List<Include> includes = g.getInclude();
        assertEquals(1,includes.size());
        
        List<String> includeRefs = new ArrayList<String>();
        for (Include i : includes) {
            includeRefs.add(i.getHref());
        }
        
        assertTrue(includeRefs.contains(uriPrefix  + "application.wadl/xsd0.xsd"));

        assertTrue(!includeRefs.contains("test:/base/application.wadl/requestMessage"));
        assertTrue(!includeRefs.contains("application.wadl/requestMessage"));

        assertTrue(!includeRefs.contains("test:/base/application.wadl/responseMessage"));
        assertTrue(!includeRefs.contains("application.wadl/responseMessage"));
        
        //
        
        WebResource responseJr = resource("application.wadl/responseMessage", false);
        String responsejsonText = responseJr.get(String.class); 
        WebResource resquestJr = resource("application.wadl/requestMessage", false);
        String requestjsonText = resquestJr.get(String.class); 
        WebResource xsdJr = resource("application.wadl/xsd0.xsd", false);
        String xsdText = xsdJr.get(String.class); 

        
        // Okay lets see if we can find the right references
        //
        
        Resource resource = a.getResources().get(0).getResource().get(0);
        
        Method post = (Method)resource.getMethodOrResource().get(0);

        List<Representation> requestRepresentations = post.getRequest().getRepresentation();
        assertEquals(hasXml ? 2 : 1, requestRepresentations.size());
        for (int counter = 0; counter < requestRepresentations.size(); counter++) {
            Representation requestRepresentation = requestRepresentations.get(counter);
            QName requestType  = requestRepresentation.getElement();
            boolean isXml = MediaType.APPLICATION_XML.equals(requestRepresentation.getMediaType());
            assertEquals("Representation should point to XML schema for both " + counter, RequestMessage.name, requestType);
            if (!isXml) {
                // Should have a the json element refernece
                String responseElementUri = requestRepresentation.getOtherAttributes().get(
                        WadlGeneratorJSONGrammarGenerator.JSON_ELEMENT_QNAME);
                assertEquals(uriPrefix + "application.wadl/requestMessage", responseElementUri);
            }
        }
        
        
        List<Representation> responseRepresentations = post.getResponse().get(0).getRepresentation();
        assertEquals(hasXml ? 2 : 1, responseRepresentations.size());
        for (int counter = 0; counter < responseRepresentations.size(); counter++) {
            Representation responseRepresentation = responseRepresentations.get(counter);
            QName responseType  = responseRepresentation.getElement();
            boolean isXml = MediaType.APPLICATION_XML.equals(responseRepresentation.getMediaType());
            assertEquals("Representation should point to XML schema for both " + counter, ResponseMessage.name, responseType);

            if (!isXml) {
                // Should have a the json element refernece
                String responseElementUri = responseRepresentation.getOtherAttributes().get(
                        WadlGeneratorJSONGrammarGenerator.JSON_ELEMENT_QNAME);
                assertEquals(uriPrefix + "application.wadl/responseMessage", responseElementUri);
            }
        }   
    }
    
    
}

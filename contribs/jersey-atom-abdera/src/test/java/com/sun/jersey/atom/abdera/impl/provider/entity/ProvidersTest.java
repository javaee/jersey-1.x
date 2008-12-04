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

package com.sun.jersey.atom.abdera.impl.provider.entity;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;

/**
 * <p>Unit tests for the <code>jersey-atom2</code> module.</p>
 */
public class ProvidersTest extends TestCase {
    
    public ProvidersTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Map<String,String> initParams = new HashMap<String,String>();
        initParams.put("com.sun.jersey.config.property.resourceConfigClass",
                       "com.sun.jersey.api.core.PackagesResourceConfig");
        initParams.put("com.sun.jersey.config.property.packages",
                       "com.sun.jersey.atom.abdera.impl.provider.entity");
        System.out.println("Starting grizzly ...");
        selectorThread = GrizzlyWebContainerFactory.create(BASE_URI, initParams);
        ClientConfig config = new DefaultClientConfig();
//        config.getClasses().add(MultiPartBeanProvider.class);
        client = Client.create(config);
    }

    @Override
    protected void tearDown() throws Exception {
        client = null;
        System.out.println("Stopping grizzly ...");
        if (selectorThread.isRunning()) {
            selectorThread.stopEndpoint();
        }
        selectorThread = null;
        super.tearDown();
    }

    Client client = null;
    SelectorThread selectorThread = null;

    private static final String BASE_URI = "http://localhost:9997/";
    private static final String[] CATEGORIES_MEDIA_TYPES_JSON = {
        "application/atomcat+json",
        "application/json",
    };
    private static final String[] CATEGORIES_MEDIA_TYPES_XML = {
        "application/atomcat+xml",
        "application/xml",
        "text/xml",
    };
    private static final String[] SERVICE_MEDIA_TYPES_JSON = {
        "application/atomsvc+json",
        "application/json",
    };
    private static final String[] SERVICE_MEDIA_TYPES_XML = {
        "application/atomsvc+xml",
        "application/xml",
        "text/xml",
    };

    public void testGetCategoriesEntity() {
        // FIXME - Abdera does not support parsing of JSON representations for Categories instances
        Categories expected = TestingFactory.createCategories();
        for (String mediaType : CATEGORIES_MEDIA_TYPES_XML) {
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("categories").accept(mediaType);
            Categories actual = builder.get(Categories.class);
            checkCategories(mediaType, expected, actual);
        }
    }

    public void testGetCategoreisString() {
        for (String mediaType : CATEGORIES_MEDIA_TYPES_JSON) {
//            System.out.println("Trying " + mediaType);
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("categories").accept(mediaType);
            String result = builder.get(String.class);
//            System.out.println(mediaType + "=" + result);
            assertTrue("Categories for media type " + mediaType + " is JSON", result.startsWith("{"));
        }
        for (String mediaType : CATEGORIES_MEDIA_TYPES_XML) {
//            System.out.println("Trying " + mediaType);
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("categories").accept(mediaType);
            String result = builder.get(String.class);
//            System.out.println(mediaType + "=" + result);
            assertTrue("Categories for media type " + mediaType + " is XML", result.startsWith("<"));
        }
    }

    public void testGetServiceEntity() {
        // FIXME - Abdera does not support parsing of JSON representations for Service instances
        Service expected = TestingFactory.createService();
        for (String mediaType : SERVICE_MEDIA_TYPES_XML) {
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("service").accept(mediaType);
            Service actual = builder.get(Service.class);
            checkService(mediaType, expected, actual);
        }
    }

    public void testGetServiceString() {
        for (String mediaType : SERVICE_MEDIA_TYPES_JSON) {
//            System.out.println("Trying " + mediaType);
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("service").accept(mediaType);
            String result = builder.get(String.class);
//            System.out.println(mediaType + "=" + result);
            assertTrue("Service for media type " + mediaType + " is JSON", result.startsWith("{"));
        }
        for (String mediaType : SERVICE_MEDIA_TYPES_XML) {
//            System.out.println("Trying " + mediaType);
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("service").accept(mediaType);
            String result = builder.get(String.class);
//            System.out.println(mediaType + "=" + result);
            assertTrue("Service for media type " + mediaType + " is XML", result.startsWith("<"));
        }
    }

    private void checkCategories(String environment, Categories expected, Categories actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " categories size",
                expected.getCategories().size(), actual.getCategories().size());
        for (Category category : expected.getCategories()) {
            Category actualCategory = actual.getCategories(category.getScheme().toString()).get(0);
            checkCategory(environment + " category scheme " + category.getScheme(), category, actualCategory);
        }
        // FIXME
    }

    private void checkCategory(String environment, Category expected, Category actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " label", expected.getLabel(), actual.getLabel());
        assertEquals(environment + " scheme", expected.getScheme().toString(), actual.getScheme().toString());
        assertEquals(environment + " term", expected.getTerm(), actual.getTerm());
    }

    private void checkCollection(String environment, Collection expected, Collection actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " title", expected.getTitle(), actual.getTitle());
        assertEquals(environment + " href", expected.getHref(), actual.getHref());
    }

    private void checkElement(String environment, Element expected, Element actual) {
        // FIXME - add tests for Element
    }

    private void checkExtensibleElement(String environment, ExtensibleElement expected, ExtensibleElement actual) {
        checkElement(environment, expected, actual);
        // FIXME - add tests for ExtensibleElement
    }

    private void checkService(String environment, Service expected, Service actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " workspaces size",
                expected.getWorkspaces().size(), actual.getWorkspaces().size());
        for (Workspace workspace : expected.getWorkspaces()) {
            Workspace actualWorkspace = actual.getWorkspace(workspace.getTitle());
            assertNotNull(environment + " workspace " + workspace.getTitle() + " exists", actualWorkspace);
            checkWorkspace(environment + " workspace " + workspace.getTitle(), workspace, actualWorkspace);
        }
    }

    private void checkWorkspace(String environment, Workspace expected, Workspace actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " title", expected.getTitle(), actual.getTitle());
        assertEquals(environment + " collections size",
                expected.getCollections().size(), actual.getCollections().size());
        for (Collection collection : expected.getCollections()) {
            Collection actualCollection = actual.getCollection(collection.getTitle());
            assertNotNull(environment + " collection " + collection.getTitle() + " exists", actualCollection);
            checkCollection(environment + " collection " + collection.getTitle(), collection, actualCollection);
        }
    }

}

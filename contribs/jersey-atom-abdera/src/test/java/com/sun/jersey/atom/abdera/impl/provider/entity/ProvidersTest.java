/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.atom.abdera.impl.provider.entity;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import com.sun.jersey.atom.abdera.ContentHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import junit.framework.TestCase;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Collection;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.ExtensibleElement;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.model.Person;
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
        config.getClasses().add(ContentBeanProvider.class);
        client = Client.create(config);
        providers = client.getProviders();
    }

    @Override
    protected void tearDown() throws Exception {
        providers = null;
        client = null;
        System.out.println("Stopping grizzly ...");
        if (selectorThread.isRunning()) {
            selectorThread.stopEndpoint();
        }
        selectorThread = null;
        super.tearDown();
    }

    private Client client = null;
    private Providers providers = null;
    private SelectorThread selectorThread = null;

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
    private static final String[] ENTRY_MEDIA_TYPES_JSON = {
        "application/atom+json",
        "application/atom+json;type=entry",
        "application/json",
    };
    private static final String[] ENTRY_MEDIA_TYPES_XML = {
        "application/atom+xml",
        "application/atom+xml;type=entry",
        "application/xml",
        "text/xml",
    };
    private static final String[] FEED_MEDIA_TYPES_JSON = {
        "application/atom+json",
        "application/atom+json;type=feed",
        "application/json",
    };
    private static final String[] FEED_MEDIA_TYPES_XML = {
        "application/atom+xml",
        "application/atom+xml;type=feed",
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

    public void testGetContent() {
        WebResource.Builder builder = client.resource(BASE_URI)
                .path("test").path("content").accept(MediaType.APPLICATION_XML_TYPE);
        Entry actual = builder.get(Entry.class);
        assertNotNull(actual);
        ContentHelper helper = new ContentHelper(providers);
        ContentBean bean = helper.getContentEntity(actual, MediaType.APPLICATION_XML_TYPE, ContentBean.class);
        assertNotNull(bean);
        assertEquals("foo value", bean.getFoo());
        assertEquals("bar value", bean.getBar());
    }

    public void testGetEntryEntity() {
        // FIXME - Abdera does not support parsing of JSON representations for Entry instances
        Entry expected = TestingFactory.createEntry();
        for (String mediaType : ENTRY_MEDIA_TYPES_XML) {
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("entry").accept(mediaType);
            Entry actual = builder.get(Entry.class);
            checkEntry(mediaType, expected, actual);
        }
    }

    public void testGetEntryString() {
        for (String mediaType : ENTRY_MEDIA_TYPES_JSON) {
//            System.out.println("Trying " + mediaType);
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("entry").accept(mediaType);
            String result = builder.get(String.class);
//            System.out.println(mediaType + "=" + result);
            assertTrue("Entry for media type " + mediaType + " is JSON", result.startsWith("{"));
        }
        for (String mediaType : ENTRY_MEDIA_TYPES_XML) {
//            System.out.println("Trying " + mediaType);
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("entry").accept(mediaType);
            String result = builder.get(String.class);
//            System.out.println(mediaType + "=" + result);
            assertTrue("Entry for media type " + mediaType + " is XML", result.startsWith("<"));
        }
    }

    public void testGetFeedEntity() {
        // FIXME - Abdera does not support parsing of JSON representations for Feed instances
        Feed expected = TestingFactory.createFeed();
        for (String mediaType : FEED_MEDIA_TYPES_XML) {
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("feed").accept(mediaType);
            Feed actual = builder.get(Feed.class);
            checkFeed(mediaType, expected, actual);
        }
    }

    public void testGetFeedString() {
        for (String mediaType : FEED_MEDIA_TYPES_JSON) {
//            System.out.println("Trying " + mediaType);
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("feed").accept(mediaType);
            String result = builder.get(String.class);
//            System.out.println(mediaType + "=" + result);
            assertTrue("Feed for media type " + mediaType + " is JSON", result.startsWith("{"));
        }
        for (String mediaType : FEED_MEDIA_TYPES_XML) {
//            System.out.println("Trying " + mediaType);
            WebResource.Builder builder = client.resource(BASE_URI)
              .path("test").path("feed").accept(mediaType);
            String result = builder.get(String.class);
//            System.out.println(mediaType + "=" + result);
            assertTrue("Feed for media type " + mediaType + " is XML", result.startsWith("<"));
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
        if (expected.getScheme() == null) {
            assertNull(environment + " scheme is null", actual.getScheme());
        } else {
            assertEquals(environment + " scheme", expected.getScheme().toString(), actual.getScheme().toString());
        }
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

    private void checkEntry(String environment, Entry expected, Entry actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " authors size",
                expected.getAuthors().size(), actual.getAuthors().size());
        for (Person author : expected.getAuthors()) {
            Person actualAuthor = findPerson(actual.getAuthors(), author.getName());
            assertNotNull(environment + " author " + author.getName() + " exists", actualAuthor);
            checkPerson(environment + " author " + author.getName(), author, actualAuthor);
        }
        assertEquals(environment + " categories size",
                expected.getCategories().size(), actual.getCategories().size());
        for (Category category : expected.getCategories()) {
            Category actualCategory = findCategory(actual.getCategories(), category.getTerm());
            assertNotNull(environment + " category " + category.getTerm() + " exists", actualCategory);
            checkCategory(environment + " category " + category.getTerm(), category, actualCategory);
        }
        // FIXME - test content, contentMimeType, contentSrc, contentType ???
        assertEquals(environment + " contributors size",
                expected.getContributors().size(), actual.getContributors().size());
        for (Person contributor : expected.getContributors()) {
            Person actualContributor = findPerson(actual.getContributors(), contributor.getName());
            assertNotNull(environment + " contributor " + contributor.getName() + " exists", actualContributor);
            checkPerson(environment + " contributor " + contributor.getName(), contributor, actualContributor);
        }
        assertEquals(environment + " id", expected.getId(), actual.getId());
        assertEquals(environment + " links size",
                expected.getLinks().size(), actual.getLinks().size());
        for (Link link : expected.getLinks()) {
            Link actualLink = findLink(actual.getLinks(), link.getRel());
            assertNotNull(environment + " link " + link.getRel() + " exists", actualLink);
            checkLink(environment + " link " + link.getRel(), link, actualLink);
        }
    }

    private void checkExtensibleElement(String environment, ExtensibleElement expected, ExtensibleElement actual) {
        checkElement(environment, expected, actual);
        // FIXME - add tests for ExtensibleElement
    }

    private void checkFeed(String environment, Feed expected, Feed actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " authors size",
                expected.getAuthors().size(), actual.getAuthors().size());
        for (Person author : expected.getAuthors()) {
            Person actualAuthor = findPerson(actual.getAuthors(), author.getName());
            assertNotNull(environment + " author " + author.getName() + " exists", actualAuthor);
            checkPerson(environment + " author " + author.getName(), author, actualAuthor);
        }
        assertEquals(environment + " categories size",
                expected.getCategories().size(), actual.getCategories().size());
        for (Category category : expected.getCategories()) {
            Category actualCategory = findCategory(actual.getCategories(), category.getTerm());
            assertNotNull(environment + " category " + category.getTerm() + " exists", actualCategory);
            checkCategory(environment + " category " + category.getTerm(), category, actualCategory);
        }
        assertEquals(environment + " contributors size",
                expected.getContributors().size(), actual.getContributors().size());
        for (Person contributor : expected.getContributors()) {
            Person actualContributor = findPerson(actual.getContributors(), contributor.getName());
            assertNotNull(environment + " contributor " + contributor.getName() + " exists", actualContributor);
            checkPerson(environment + " contributor " + contributor.getName(), contributor, actualContributor);
        }
        assertEquals(environment + " entries size",
                expected.getEntries().size(), actual.getEntries().size());
        for (Entry entry : expected.getEntries()) {
            Entry actualEntry = findEntry(actual.getEntries(), entry.getId());
            assertNotNull(environment + " entry " + entry.getId() + " exists", actualEntry);
            checkEntry(environment + " entry " + entry.getId(), entry, actualEntry);
        }
        assertEquals(environment + " id", expected.getId(), actual.getId());
        assertEquals(environment + " links size",
                expected.getLinks().size(), actual.getLinks().size());
        for (Link link : expected.getLinks()) {
            Link actualLink = findLink(actual.getLinks(), link.getRel());
            assertNotNull(environment + " link " + link.getRel() + " exists", actualLink);
            checkLink(environment + " link " + link.getRel(), link, actualLink);
        }
    }

    private void checkLink(String environment, Link expected, Link actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " href", expected.getHref(), actual.getHref());
        assertEquals(environment + " hrefLang", expected.getHrefLang(), actual.getHrefLang());
        assertEquals(environment + " length", expected.getLength(), actual.getLength());
        if (expected.getMimeType() == null) {
            assertNull(environment + " mimeType does not exist", actual.getMimeType());
        } else {
            assertEquals(environment + " mimeType", expected.getMimeType().toString(), actual.getMimeType().toString());
        }
        assertEquals(environment + " rel", expected.getRel(), actual.getRel());
        assertEquals(environment + " title", expected.getTitle(), actual.getTitle());
    }

    private void checkPerson(String environment, Person expected, Person actual) {
        checkExtensibleElement(environment, expected, actual);
        assertEquals(environment + " email", expected.getEmail(), actual.getEmail());
        assertEquals(environment + " name", expected.getName(), actual.getName());
        assertEquals(environment + " IRI", expected.getUri(), actual.getUri());
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

    private Category findCategory(List<Category> categories, String term) {
        for (Category category : categories) {
            if (term.equals(category.getTerm())) {
                return category;
            }
        }
        return null;
    }

    private Entry findEntry(List<Entry> entries, IRI id) {
        for (Entry entry : entries) {
            if (id.equals(entry.getId())) {
                return entry;
            }
        }
        return null;
    }

    private Link findLink(List<Link> links, String rel) {
        for (Link link : links) {
            if (rel.equals(link.getRel())) {
                return link;
            }
        }
        return null;
    }

    private Person findPerson(List<Person> persons, String name) {
        for (Person person : persons) {
            if (name.equals(person.getName())) {
                return person;
            }
        }
        return null;
    }

}

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

package com.sun.jersey.atom.abdera.impl.provider.entity;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Service;
import org.apache.abdera.model.Workspace;

/**
 * <p>Shared helper classes for the unit test suite.</p>
 */
public class AtomTestingFactory {


    private static Abdera abdera = Abdera.getInstance();


    /**
     * <p>Create and return a populated {@link Categories} instance.</p>
     */
    public static Categories createCategories() {
        Categories categories = abdera.newCategories();
        categories.addCategory("http://example.com/categories/foo", "foo", "This is the 'foo' category");
        categories.addCategory("http://example.com/categories/bar", "bar", "This is the 'bar' category");
        categories.addCategory("http://example.com/categories/baz", "baz", "This is the 'baz' category");
        categories.addCategory("http://example.com/categories/bop", "bop", "This is the 'bop' category");
        return categories;
    }

    /**
     * <p>Create and return a populated {@link Entry} instance with
     * no suffixes added.</p>
     */
    public static Entry createEntry() {
        return createEntry("");
    }

    /**
     * <p>Create and return a populated {@link Entry} instance with
     * the specified suffixes added.</p>
     */
    public static Entry createEntry(String suffix) {
        Entry entry = abdera.newEntry();
        entry.addAuthor("Entry Author 1" + suffix);
        entry.addAuthor("Entry Author 2" + suffix);
        entry.addCategory("Entry Category 1" + suffix);
        entry.addCategory("Entry Category 2" + suffix);
        entry.addCategory("Entry Category 3" + suffix);
        entry.addContributor("Entry Contributor 1" + suffix);
        entry.setId("EntryId" + suffix.replace(' ', '_'));
        entry.addLink("http://example.com/entry", "self", "application/xml", "Entry Self Link" + suffix, null, 0);
        entry.addLink("http://example.com/entry", "edit", "application/xml", "Entry Edit Link" + suffix, null, 0);
        // FIXME - should we set a default content element?
        return entry;
    }

    /**
     * <p>Create and return a populated {@link Feed} instance with
     * no suffixes added.</p>
     */
    public static Feed createFeed() {
        return createFeed("");
    }

    /**
     * <p>Create and return a populated {@link Feed} instance with
     * the specified suffixes added.</p>
     */
    public static Feed createFeed(String suffix) {
        Feed feed = abdera.newFeed();
        feed.addAuthor("Feed Author 1" + suffix);
        feed.addAuthor("Feed Author 2" + suffix);
        feed.addAuthor("Feed Author 3" + suffix);
        feed.addCategory("Feed Category 1" + suffix);
        feed.addCategory("Feed Category 2" + suffix);
        feed.addContributor("Feed Contributor 1" + suffix);
        feed.addContributor("Feed Contributor 2" + suffix);
        feed.addContributor("Feed Contributor 3" + suffix);
        feed.addContributor("Feed Contributor 4" + suffix);
        feed.addEntry(createEntry(suffix + " Entry 1"));
        feed.addEntry(createEntry(suffix + " Entry 2"));
        feed.addEntry(createEntry(suffix + " Entry 3"));
        feed.addEntry(createEntry(suffix + " Entry 4"));
        feed.setId("FeedId" + suffix.replace(' ', '_'));
        feed.addLink("http://example.com/feed", "self", "application/xml", "Feed Self Link" + suffix, null, 0);
        return feed;
    }

    /**
     * <p>Create and return a populated {@link Service} instance.</p>
     */
    public static Service createService() {
        Service service = abdera.newService();
        Workspace workspace1 = service.addWorkspace("workspace1");
        workspace1.addCollection("collection11", "http://example.com/collection11").
          setAccept("x-workspace1/x-collection1+xml", "x-workspace1/x-collection1+json");
        workspace1.addCollection("collection12", "http://example.com/collection12").
          setAccept("x-workspace1/x-collection2+xml", "x-workspace1/x-collection2+json");
        workspace1.addCollection("collection13", "http://example.com/collection13").
          setAccept("x-workspace1/x-collection3+xml", "x-workspace1/x-collection3+json");
        Workspace workspace2 = service.addWorkspace("workspace2");
        workspace2.addCollection("collection21", "http://example.com/collection21").
          setAccept("x-workspace1/x-collection1+xml", "x-workspace1/x-collection1+json");
        workspace2.addCollection("collection22", "http://example.com/collection22").
          setAccept("x-workspace1/x-collection2+xml", "x-workspace1/x-collection2+json");
        workspace2.addCollection("collection23", "http://example.com/collection23").
          setAccept("x-workspace1/x-collection3+xml", "x-workspace1/x-collection3+json");
        return service;
    }

}

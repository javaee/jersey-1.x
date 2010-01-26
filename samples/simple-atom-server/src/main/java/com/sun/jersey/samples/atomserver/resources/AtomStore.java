/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.samples.atomserver.resources;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
class AtomStore {
    private static final MediaType atomMediaType = new MediaType("application", "atom+xml");

    static String getCollectionPath() {
        return "collection";
    }
    
    static String getFeedPath() {
        return "collection/feed.xml";
    }
    
    static String getPath(String id) {
        return "collection/" + id;
    }
    
    static String getEntryPath(String id) {
        return getPath(id) + "/entry.xml";
    }
    
    static String getMediaPath(String id) {
        return getPath(id) + "/media";
    }
    
    static boolean hasMedia(String id) {
        String p = getMediaPath(id);
        return FileStore.FS.exists(p);
    }
    
    static void checkExistence(String path) {
        if (!FileStore.FS.exists(path))
            throw new NotFoundException("Entry does not exist");
    }    
    
    static Feed getFeedDocument() throws FeedException {
        InputStream in = null;
        
        synchronized(FileStore.FS) {
            in = FileStore.FS.getFileContents(AtomStore.getFeedPath());
        }
        
        WireFeedInput input = new WireFeedInput();
        WireFeed wireFeed = input.build(new InputStreamReader(in));
        return (Feed)wireFeed;
    }
    
    static Feed getFeedDocument(MessageBodyWorkers bodyContext, URI feedUri) throws IOException, FeedException {
        InputStream in = null;
        
        synchronized(FileStore.FS) {
            in = FileStore.FS.getFileContents(AtomStore.getFeedPath());
            if (in == null) {
                in = createDefaultFeedDocument(bodyContext, feedUri.toString());
            }
        }
        
        WireFeedInput input = new WireFeedInput();
        WireFeed wireFeed = input.build(new InputStreamReader(in));
        return (Feed)wireFeed;
    }
        
    static void updateFeedDocumentWithNewEntry(MessageBodyWorkers bodyContext, 
            Feed f, Entry e) throws IOException {
        f.getEntries().add(e);
        updateFeedDocument(bodyContext, f);
    }
        
    static void updateFeedDocumentRemovingEntry(MessageBodyWorkers bodyContext, 
            Feed f, String id) throws IOException {
        Entry e = findEntry(id, f);
        f.getEntries().remove(e);

        updateFeedDocument(bodyContext, f);
    }
    
    static void updateFeedDocumentWithExistingEntry(MessageBodyWorkers bodyContext, 
            Feed f, Entry e) throws IOException {
        Entry old = findEntry(e.getId(), f);
        f.getEntries().remove(old);
        f.getEntries().add(0, e);

        updateFeedDocument(bodyContext, f);
    }
        
    static Entry findEntry(String id, Feed f) {
        List l = f.getEntries();
        for (Object o : l) {
            Entry e = (Entry)o;
            if (id.equals(e.getId())) 
                return e;
        }
        
        return null;        
    }
    
    static void addLink(Entry e, String rel, URI uri) {
        Link l = new Link();
        l.setRel(rel);
        l.setHref(uri.toString());
        e.getOtherLinks().add(l);
    }    
    
    static String getLink(Entry e, String rel) {
        List links = e.getOtherLinks();
        
        Link l = null;
        for (Object o : links) {
            l = (Link)o;
            if (l.getRel().equals(rel)) {
                return l.getHref();
            }
        }
        return null;
    }    
    
    static void updateLink(Entry e, String rel, URI uri) {
        List links = e.getOtherLinks();
        
        Link l = null;
        for (Object o : links) {
            l = (Link)o;
            if (l.getRel().equals(rel)) {
                links.remove(l);
                break;
            }
        }
        
        l = new Link();
        l.setRel(rel);
        l.setHref(uri.toString());
        links.add(l);
    }    
    
    static void updateFeedDocument(MessageBodyWorkers bodyContext, Feed f) throws IOException {
        MessageBodyWriter<Feed> ep = bodyContext.getMessageBodyWriter(
                Feed.class, null,
                null, atomMediaType);
        synchronized(FileStore.FS) {
            ep.writeTo(f, f.getClass(), null, null, atomMediaType, null, FileStore.FS.getFileOutputStream(AtomStore.getFeedPath()));
        }
    }
            
    static void createEntryDocument(MessageBodyWorkers bodyContext, String id, Entry e) throws IOException {
        MessageBodyWriter<Entry> ep = bodyContext.getMessageBodyWriter(
                Entry.class, null,
                null, atomMediaType);
        String path = AtomStore.getEntryPath(id);
        ep.writeTo(e, e.getClass(), null, null, atomMediaType, null, FileStore.FS.getFileOutputStream(path));
    }
    
    static void createMediaDocument(String id, byte[] content) throws IOException {
        String path = AtomStore.getMediaPath(id);
        OutputStream o = FileStore.FS.getFileOutputStream(path);
        o.write(content);
        o.close();
    }
    
    static void deleteEntry(String entryId) {
        FileStore.FS.deleteDirectory(getPath(entryId));
    }
    
    static InputStream createDefaultFeedDocument(MessageBodyWorkers bodyContext,
            String uri) throws IOException {
        Feed f = new Feed();
        f.setTitle("Feed");
        
        Link selfLink = new Link();
        selfLink.setRel("self");
        selfLink.setHref(uri);
        f.getOtherLinks().add(selfLink);
        
        MessageBodyWriter<Feed> ep = bodyContext.getMessageBodyWriter(
                Feed.class, null,
                null, atomMediaType);
        ep.writeTo(f, f.getClass(), null, null, atomMediaType, null, FileStore.FS.getFileOutputStream(AtomStore.getFeedPath()));
        
        return FileStore.FS.getFileContents(AtomStore.getFeedPath());
    }

    static Entry createDefaulMediaLinkEntryDocument() {
        Entry e = new Entry();
        e.setTitle("Media Entry");
        Date d = new Date();
        e.setCreated(d);
        e.setUpdated(d);
        return e;
    }    
    
}

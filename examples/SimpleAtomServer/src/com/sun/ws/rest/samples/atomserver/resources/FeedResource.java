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

package com.sun.ws.rest.samples.atomserver.resources;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@UriTemplate("/collection/")
@ProduceMime("application/atom+xml")
public class FeedResource {
    @HttpContext UriInfo uriInfo;

    
    @UriTemplate("{entry}")
    public EntryResource getEntryResource(@UriParam("entry") String entryId) {
        return new EntryResource(entryId);
    }
    
    @UriTemplate("edit/{entry}")
    public EntryResource getEditEntryResource(@UriParam("entry") String entryId) {
        return new EditEntryResource(entryId, uriInfo);
    }
    
    private URI getEditURI(String path) {
        return getURI("edit/" + path);
    }
        
//   Uncomment the following methods, and comment out the above equivalent 
//   methods, to support optimisitic concurrency when updating entries
    
//    @UriTemplate("edit/{version}/{entry}")
//    public EntryResource getEditEntryResource(
//            @UriParam("entry") String entryId,
//            @UriParam("version") int version) throws FeedException {
//        return new EditOptimisiticEntryResource(entryId, version, uriInfo);
//    }
//    
//    private URI getEditURI(String path) {
//        return getURI("edit/0/" + path);
//    }
    
    private URI getURI(String path) {
        return uriInfo.getURI().resolve(path);
    }    
    
    
    @HttpMethod
    public Feed getFeed() throws IOException, FeedException {
        return AtomStore.getFeedDocument(uriInfo.getURI());
    }

    @HttpMethod
    @ConsumeMime("application/atom+xml")
    public Response postEntry(Entry e) throws IOException, FeedException {
        // Get the next unique name of the entry
        String entryId = FileStore.FS.getNextId();
        
        // Set the self link 
        URI entryUri = getURI(entryId);
        AtomStore.addLink(e, "self", entryUri.toString());
        
        // Set the edit link
        URI editEntryUri = getEditURI(entryId);
        AtomStore.addLink(e, "edit", editEntryUri.toString());

        // Set the id
        e.setId(entryId);
                
        // Store the entry document 
        AtomStore.createEntryDocument(entryId, e);

        // Update the feed document with the entry
        Feed f = AtomStore.getFeedDocument(uriInfo.getURI());
        AtomStore.updateFeedDocumentWithNewEntry(f, e);
        
        // Return 201 Created
        return Response.Builder.created(e, entryUri).build();
    }

    @HttpMethod
    public Response postMediaEntry(
            @HttpContext HttpHeaders headers,
            byte[] entry) throws IOException, FeedException {
        // Get the next unique name of the entry
        String entryId = FileStore.FS.getNextId();

        // Create a default entry
        Entry e = AtomStore.createDefaulMediaLinkEntryDocument();
        
        URI mediaUri = getURI(entryId + "/media");
        
        // Set the self link
        URI entryUri = getURI(entryId);
        AtomStore.addLink(e, "self", entryUri.toString());
        
        // Set the edit link
        URI editEntryUri = getEditURI(entryId);
        AtomStore.addLink(e, "edit", editEntryUri.toString());
        
        // Set the edit-media link
        URI editMediaUri = getEditURI(entryId + "/media");
        AtomStore.addLink(e, "edit-media", editMediaUri.toString());        
        
        // Set the id
        e.setId(entryId);
        
        // Set the content to link to the media
        Content c = new Content();
        c.setType(headers.getMediaType().toString());
        // Set the link source
        c.setSrc(mediaUri.toString());
        e.getContents().add(c);
        
        // Store entry document 
        AtomStore.createEntryDocument(entryId, e);
        // Store the media
        AtomStore.createMediaDocument(entryId, entry);
        
        // Update the feed document with the entry
        Feed f = AtomStore.getFeedDocument(uriInfo.getURI());
        AtomStore.updateFeedDocumentWithNewEntry(f, e);
        
        // Return 201 Created
        return Response.Builder.created(e, entryUri).build();
    }
    
}

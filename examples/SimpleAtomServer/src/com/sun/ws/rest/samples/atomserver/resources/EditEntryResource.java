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

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EditEntryResource extends EntryResource {
    protected UriInfo uriInfo;

    public EditEntryResource(String entryId, UriInfo uriInfo) {
        super(entryId);
        this.uriInfo = uriInfo;
    }
    
    @HttpMethod
    public Entry putEntry(Entry e) throws IOException, FeedException {
        UriBuilder entryUri = uriInfo.getBaseUriBuilder().
                path(FeedResource.class).
                path(entryId);
        AtomStore.updateLink(e, "self", entryUri.build());
        
        UriBuilder editEntryUri = getUriBuilder();
        AtomStore.updateLink(e, "edit", editEntryUri.build());
        
        if (AtomStore.hasMedia(entryId)) {
            // TODO update content link
            URI mediaUri = entryUri.path("media").build();
            
            URI editMediaUri = editEntryUri.path("media").build();
            AtomStore.updateLink(e, "edit-media", editMediaUri);
        }
        e.setId(entryId);
        e.setForeignMarkup(null);

        // Write out the entry document 
        AtomStore.createEntryDocument(entryId, e);

        // Update the feed document with the entry
        Feed f = AtomStore.getFeedDocument();
        AtomStore.updateFeedDocumentWithExistingEntry(f, e);
        
        return e;
    }

    
    @HttpMethod
    public void deleteEntry() throws IOException, FeedException {
        AtomStore.deleteEntry(entryId);
        
        Feed f = AtomStore.getFeedDocument();
        AtomStore.updateFeedDocumentRemovingEntry(f, entryId);
    }
    
    
    @HttpMethod
    @Path("media")
    @ConsumeMime("*/*")
    public void putMedia(@HttpContext HttpHeaders headers,
            byte[] update) throws IOException, FeedException {
        // Check if media exists, otherwise 404
        String mediaPath = AtomStore.getMediaPath(entryId);
        AtomStore.checkExistence(mediaPath);

        // Get the atom entry
        Feed f = AtomStore.getFeedDocument();
        Entry e = AtomStore.findEntry(entryId, f);
        
        // Update the entry
        e.setUpdated(new Date());
        UriBuilder editMediaEntryUri = getUriBuilder();
        AtomStore.updateLink(e, "edit-media", 
                editMediaEntryUri.build());
        editMediaEntryUri.path("..");
        AtomStore.updateLink(e, "edit", 
                editMediaEntryUri.build().normalize());
        
        // TODO update the content type
        
        // Write out the feed document
        AtomStore.updateFeedDocument(f);
        // Write out the entry document
        AtomStore.createEntryDocument(entryId, e);
        // Write out the media
        AtomStore.createMediaDocument(entryId, update);
    }
        
    protected UriBuilder getUriBuilder() {
        return uriInfo.getAbsolutePathBuilder();
    }
}

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

package com.sun.jersey.samples.atomserver.resources;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.jersey.spi.container.MessageBodyContext;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@Path("/collection")
@ProduceMime("application/atom+xml")
public class FeedResource {
    @Context UriInfo uriInfo;

    @Context MessageBodyContext bodyContext;
    
    @Path("{entry}")
    public EntryResource getEntryResource(@PathParam("entry") String entryId) {
        return new EntryResource(entryId);
    }
    
    @Path("edit/{entry}")
    public EntryResource getEditEntryResource(@PathParam("entry") String entryId) {
        return new EditEntryResource(entryId, uriInfo, bodyContext);
    }
        
    private UriBuilder getEditUriBuilder() {
        return uriInfo.getAbsolutePathBuilder().path("edit");
    }
        
//   Uncomment the following methods, and comment out the above equivalent 
//   methods, to support optimisitic concurrency when updating entries
    
//    @Path("edit/{version}/{entry}")
//    public EntryResource getEditEntryResource(
//            @PathParam("entry") String entryId,
//            @PathParam("version") int version) throws FeedException {
//        return new EditOptimisiticEntryResource(entryId, version, 
//                uriInfo, bodyContext);
//    }
//    
//    private UriBuilder getEditUriBuilder() {
//        return uriInfo.getAbsolutePathBuilder().path("edit/0");
//    }
    
    
    @GET
    public Feed getFeed() throws IOException, FeedException {
        return AtomStore.getFeedDocument(bodyContext, uriInfo.getAbsolutePath());
    }

    @POST
    @ConsumeMime("application/atom+xml")
    public Response postEntry(Entry e) throws IOException, FeedException {
        // Get the next unique name of the entry
        String entryId = FileStore.FS.getNextId();
        
        // Set the self link 
        URI entryUri = uriInfo.getAbsolutePathBuilder().
                path(entryId).build();
        AtomStore.addLink(e, "self", entryUri);
        
        // Set the edit link
        URI editEntryUri = getEditUriBuilder().
                path(entryId).build();
        AtomStore.addLink(e, "edit", editEntryUri);

        // Set the id
        e.setId(entryId);
                
        // Store the entry document 
        AtomStore.createEntryDocument(bodyContext, entryId, e);

        // Update the feed document with the entry
        Feed f = AtomStore.getFeedDocument(bodyContext, uriInfo.getAbsolutePath());
        AtomStore.updateFeedDocumentWithNewEntry(bodyContext, f, e);
        
        // Return 201 Created
        return Response.created(entryUri).entity(e).build();
    }

    @POST
    public Response postMediaEntry(
            @Context HttpHeaders headers,
            byte[] entry) throws IOException, FeedException {
        // Get the next unique name of the entry
        String entryId = FileStore.FS.getNextId();

        // Create a default entry
        Entry e = AtomStore.createDefaulMediaLinkEntryDocument();
                
        UriBuilder entryUriBuilder = uriInfo.getAbsolutePathBuilder().path(entryId);
        UriBuilder editEntryUriBuilder = getEditUriBuilder().path(entryId);
        
        // Set the self link
        URI entryUri = entryUriBuilder.build();        
        AtomStore.addLink(e, "self", entryUri);
        
        // Set the edit link
        URI editEntryUri = editEntryUriBuilder.build();
        AtomStore.addLink(e, "edit", editEntryUri);
        
        // Set the edit-media link
        URI editMediaUri = editEntryUriBuilder.
                path("media").build();
        AtomStore.addLink(e, "edit-media", editMediaUri);        
        
        // Set the id
        e.setId(entryId);
        
        // Set the content to link to the media
        Content c = new Content();
        c.setType(headers.getMediaType().toString());
        URI mediaUri = entryUriBuilder.
                path("media").
                build();
        c.setSrc(mediaUri.toString());
        e.getContents().add(c);
        
        // Store entry document 
        AtomStore.createEntryDocument(bodyContext, entryId, e);
        // Store the media
        AtomStore.createMediaDocument(entryId, entry);
        
        // Update the feed document with the entry
        Feed f = AtomStore.getFeedDocument(bodyContext, uriInfo.getAbsolutePath());
        AtomStore.updateFeedDocumentWithNewEntry(bodyContext, f, e);
        
        // Return 201 Created
        return Response.created(entryUri).entity(e).build();
    }
    
}

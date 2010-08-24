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

package com.sun.jersey.samples.atomserver.resources;

import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import java.io.IOException;
import java.net.URI;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
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
@Produces("application/atom+xml")
public class FeedResource {
    @Context UriInfo uriInfo;

    @Context MessageBodyWorkers bodyContext;
    
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
    @Consumes("application/atom+xml")
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

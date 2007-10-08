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

package com.sun.ws.rest.samples.bookmark.resources;

import com.sun.ws.rest.samples.bookmark.entities.BookmarkEntity;
import com.sun.ws.rest.samples.bookmark.util.tx.TransactionManager;
import com.sun.ws.rest.samples.bookmark.util.tx.Transactional;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Jakub Podlesak, Paul Sandoz
 */
public class BookmarksResource {
    
    UriInfo uriInfo; // actual uri info
    EntityManager em; // entity manager provided by parent resource
    
    UserResource userResource; // parent user resource
    
    /** Creates a new instance of BookmarksResource */
    public BookmarksResource(UriInfo uriInfo, 
            EntityManager em, UserResource userResource) {
        this.uriInfo = uriInfo;
        this.em = em;
        this.userResource = userResource;
    }
    
    public Collection<BookmarkEntity> getBookmarks() {
        return userResource.getUserEntity().getBookmarkEntityCollection();
    }
    
    @UriTemplate(value = "{bmid}", limited = false)
    public BookmarkResource getBookmark(@UriParam("bmid") String bmid) {
        return new BookmarkResource(uriInfo, em, 
                userResource.getUserEntity(), bmid);
    }
    
    @HttpMethod("GET")
    @ProduceMime("application/json")
    public JSONArray getBookmarksAsJsonArray() {
        JSONArray uriArray = new JSONArray();
        UriBuilder ub = null;
        for (BookmarkEntity bookmarkEntity : getBookmarks()) {
            ub = (ub == null) ? uriInfo.getBuilder() : ub.clone();
            URI bookmarkUri = ub.
                    path(bookmarkEntity.getBookmarkEntityPK().getBmid()).
                    build();
            uriArray.put(bookmarkUri.toString());
        }
        return uriArray;
    }
    
    @HttpMethod("POST")
    @ConsumeMime("application/json")
    public Response postForm(JSONObject bookmark) {
        try {
            final BookmarkEntity bookmarkEntity = new BookmarkEntity(
                    getBookmarkId(bookmark.getString("uri")), 
                    userResource.getUserEntity().getUserid());
            
            bookmarkEntity.setUri(bookmark.getString("uri"));
            bookmarkEntity.setUpdated(new Date());
            bookmarkEntity.setSdesc(bookmark.getString("sdesc"));
            bookmarkEntity.setLdesc(bookmark.getString("ldesc"));
            userResource.getUserEntity().getBookmarkEntityCollection().add(bookmarkEntity);
            
            TransactionManager.manage(new Transactional(em) { public void transact() {
                em.merge(userResource.getUserEntity());
            }});
            
            URI bookmarkUri = uriInfo.getBuilder().
                    path(bookmarkEntity.getBookmarkEntityPK().getBmid()).
                    build();
            return Response.Builder.created(bookmarkUri).build();
        } catch (JSONException jsone) {
            throw new WebApplicationException(jsone);
        }
    }

    private String getBookmarkId(String uri) {
        return uri;
    }
}

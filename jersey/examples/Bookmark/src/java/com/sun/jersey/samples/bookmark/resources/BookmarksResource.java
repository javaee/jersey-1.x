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

package com.sun.jersey.samples.bookmark.resources;

import com.sun.jersey.samples.bookmark.entities.BookmarkEntity;
import com.sun.jersey.samples.bookmark.util.tx.TransactionManager;
import com.sun.jersey.samples.bookmark.util.tx.Transactional;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
    
    @Path(value = "{bmid}", limited = false)
    public BookmarkResource getBookmark(@PathParam("bmid") String bmid) {
        return new BookmarkResource(uriInfo, em, 
                userResource.getUserEntity(), bmid);
    }
    
    @GET
    @Produces("application/json")
    public JSONArray getBookmarksAsJsonArray() {
        JSONArray uriArray = new JSONArray();
        UriBuilder ub = null;
        for (BookmarkEntity bookmarkEntity : getBookmarks()) {
            ub = (ub == null) ? uriInfo.getAbsolutePathBuilder() : ub.clone();
            URI bookmarkUri = ub.
                    path(bookmarkEntity.getBookmarkEntityPK().getBmid()).
                    build();
            uriArray.put(bookmarkUri.toASCIIString());
        }
        return uriArray;
    }
    
    @POST
    @Consumes("application/json")
    public Response postForm(JSONObject bookmark) throws JSONException {
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

        URI bookmarkUri = uriInfo.getAbsolutePathBuilder().
                path(bookmarkEntity.getBookmarkEntityPK().getBmid()).
                build();
        return Response.created(bookmarkUri).build();
    }

    private String getBookmarkId(String uri) {
        return uri;
    }
}

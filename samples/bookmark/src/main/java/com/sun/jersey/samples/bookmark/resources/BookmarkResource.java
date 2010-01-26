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

package com.sun.jersey.samples.bookmark.resources;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.samples.bookmark.entities.BookmarkEntity;
import com.sun.jersey.samples.bookmark.entities.BookmarkEntityPK;
import com.sun.jersey.samples.bookmark.entities.UserEntity;
import com.sun.jersey.samples.bookmark.util.tx.TransactionManager;
import com.sun.jersey.samples.bookmark.util.tx.Transactional;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Jakub Podlesak, Paul Sandoz
 */
public class BookmarkResource {
    
    UriInfo uriInfo; // actual uri info provided by parent resource
    EntityManager em; // entity manager provided by parent resource
    
    BookmarkEntity bookmarkEntity;
    
    /** Creates a new instance of UserResource */
    public BookmarkResource(UriInfo uriInfo, EntityManager em, 
            UserEntity userEntity, String bmid) {
        this.uriInfo = uriInfo;
        this.em = em;
        bookmarkEntity = em.find(BookmarkEntity.class, 
                new BookmarkEntityPK(bmid, userEntity.getUserid()));
        if (null == bookmarkEntity) {
            throw new NotFoundException("bookmark with userid=" + 
                    userEntity.getUserid() + " and bmid=" + 
                    bmid + " not found\n");
        }
        bookmarkEntity.setUserEntity(userEntity);
    }
    
    @GET
    @Produces("application/json")
    public JSONObject getBookmark() {
        return asJson();
    }
    
    @PUT
    @Consumes("application/json")
    public void putBookmark(JSONObject jsonEntity) throws JSONException {

        bookmarkEntity.setLdesc(jsonEntity.getString("ldesc"));
        bookmarkEntity.setSdesc(jsonEntity.getString("sdesc"));
        bookmarkEntity.setUpdated(new Date());
        
        TransactionManager.manage(new Transactional(em) { public void transact() {
            em.merge(bookmarkEntity);
        }});
    }    
    
    @DELETE
    public void deleteBookmark() {
        TransactionManager.manage(new Transactional(em) { public void transact() {
            UserEntity userEntity = bookmarkEntity.getUserEntity();
            userEntity.getBookmarkEntityCollection().remove(bookmarkEntity);
            em.merge(userEntity);
            em.remove(bookmarkEntity);
        }});
    }    
    
    
    public String asString() {
        return toString();
    }
    
    public JSONObject asJson() {
        try {
            return new JSONObject()
            .put("userid", bookmarkEntity.getBookmarkEntityPK().getUserid())
            .put("sdesc", bookmarkEntity.getSdesc())
            .put("ldesc", bookmarkEntity.getLdesc())
            .put("uri", bookmarkEntity.getUri());
        } catch (JSONException je){
            return null;
        }
    }
    
    public String toString() {
        return bookmarkEntity.getBookmarkEntityPK().getUserid();
    }
}

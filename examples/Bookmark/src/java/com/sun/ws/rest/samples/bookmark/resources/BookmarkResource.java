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

import com.sun.ws.rest.api.NotFoundException;
import com.sun.ws.rest.samples.bookmark.entities.BookmarkEntity;
import com.sun.ws.rest.samples.bookmark.entities.BookmarkEntityPK;
import com.sun.ws.rest.samples.bookmark.entities.UserEntity;
import com.sun.ws.rest.samples.bookmark.util.tx.TransactionManager;
import com.sun.ws.rest.samples.bookmark.util.tx.Transactional;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
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
    
    @HttpMethod("GET")
    @ProduceMime("application/json")
    public JSONObject getBookmark() {//@UriParam("userid") String userid) {
        return asJson();
    }
    
    @HttpMethod("PUT")
    @ConsumeMime("application/json")
    public void putBookmark(JSONObject jsonEntity) throws JSONException {

        bookmarkEntity.setLdesc(jsonEntity.getString("ldesc"));
        bookmarkEntity.setSdesc(jsonEntity.getString("sdesc"));
        bookmarkEntity.setUpdated(new Date());
        
        TransactionManager.manage(new Transactional(em) { public void transact() {
            em.merge(bookmarkEntity);
        }});
    }    
    
    @HttpMethod("DELETE")
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

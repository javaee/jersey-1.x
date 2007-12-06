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
import com.sun.ws.rest.samples.bookmark.entities.UserEntity;
import com.sun.ws.rest.samples.bookmark.util.tx.TransactionManager;
import com.sun.ws.rest.samples.bookmark.util.tx.Transactional;
import javax.persistence.EntityManager;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Jakub Podlesak, Paul Sandoz
 */
public class UserResource {
    
    String userid; // userid from url
    UserEntity userEntity; // appropriate jpa user entity
    
    UriInfo uriInfo; // actual uri info provided by parent resource
    EntityManager em; // entity manager provided by parent resource
    
    /** Creates a new instance of UserResource */
    public UserResource(UriInfo uriInfo, EntityManager em, String userid) {
        this.uriInfo = uriInfo;
        this.userid = userid;
        this.em = em;
        userEntity = em.find(UserEntity.class, userid);
    }
    
    @Path("bookmarks/")
    public BookmarksResource getBookmarksResource() {
        if (null == userEntity) {
            throw new NotFoundException("userid " + userid + " does not exist!");
        }
        return new BookmarksResource(uriInfo, em, this);
    }
    
    
    @GET
    @ProduceMime("application/json")
    public JSONObject getUser() throws JSONException {
        if (null == userEntity) {
            throw new NotFoundException("userid " + userid + "does not exist!");
        }
        return new JSONObject()
            .put("userid", userEntity.getUserid())
            .put("username", userEntity.getUsername())
            .put("email", userEntity.getEmail())
            .put("password", userEntity.getPassword())
            .put("bookmarks", uriInfo.getAbsolutePathBuilder().path("bookmarks").build());
    }
    
    @PUT
    @ConsumeMime("application/json")
    public Response putUser(JSONObject jsonEntity) throws JSONException {
        
        String jsonUserid = jsonEntity.getString("userid");
        
        if ((null != jsonUserid) && !jsonUserid.equals(userid)) {
            return Response.status(409).entity("userids differ!\n").build();
        }
        
        final boolean newRecord = (null == userEntity); // insert or update ?
        
        if (newRecord) { // new user record to be inserted
            userEntity = new UserEntity();
            userEntity.setUserid(userid);
        }
        userEntity.setUsername(jsonEntity.getString("username"));
        userEntity.setEmail(jsonEntity.getString("email"));
        userEntity.setPassword(jsonEntity.getString("password"));
        
        if (newRecord) {
            TransactionManager.manage(new Transactional(em) { public void transact() {
                em.persist(userEntity);
            }});
            return Response.created(uriInfo.getAbsolutePath()).build();
        } else {
            TransactionManager.manage(new Transactional(em) { public void transact() {
                em.merge(userEntity);
            }});
            return Response.noContent().build();
        }
    }
    
    @DELETE
    public void deleteUser() {
        if (null == userEntity) {
            throw new NotFoundException("userid " + userid + "does not exist!");
        }
        TransactionManager.manage(new Transactional(em) { public void transact() {
            em.remove(userEntity);
        }});
    }
    
    
    public String asString() {
        return toString();
    }
    
    public String toString() {
        return userEntity.getUserid();
    }
    
    public UserResource(UserEntity userEntity) {
        this.userEntity = userEntity;
    }
    
    public UserEntity getUserEntity() {
        return userEntity;
    }
}

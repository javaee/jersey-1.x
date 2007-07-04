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
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.json.JSONException;
import org.json.JSONObject;

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
    
    @UriTemplate("bookmarks/")
    public BookmarksResource getBookmarksResource() {
        if (null == userEntity) {
            throw new NotFoundException("userid " + userid + " does not exist!");
        }
        return new BookmarksResource(uriInfo, em, this);
    }
    
    
    @HttpMethod("GET")
    @ProduceMime("application/json")
    public JSONObject getUser() {
        if (null == userEntity) {
            throw new NotFoundException("userid " + userid + "does not exist!");
        }
        return asJson();
    }
    
    @HttpMethod("PUT")
    @ConsumeMime("application/json")
    public Response putUser(JSONObject jsonEntity) throws JSONException {
        
        String jsonUserid = jsonEntity.getString("userid");
        Response.Builder rBuilder = Response.Builder.noContent();
        
        if ((null != jsonUserid) && !jsonUserid.equals(userid)) {
            rBuilder.status(409); 
            rBuilder.representation("userids differ!\n");
            return rBuilder.build();
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
            rBuilder.created(uriInfo.getURI());
         } else {
            TransactionManager.manage(new Transactional(em) { public void transact() {
                em.merge(userEntity);
            }});
            rBuilder.status(204);
          }
        return rBuilder.build();
    }
    
    @HttpMethod("DELETE")
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
    
    public JSONObject asJson() {
        try {
            return new JSONObject()
            .put("userid", userEntity.getUserid())
            .put("username", userEntity.getUsername())
            .put("email", userEntity.getEmail())
            .put("password", userEntity.getPassword());
        } catch (JSONException je){
            throw new WebApplicationException(je);
        }
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

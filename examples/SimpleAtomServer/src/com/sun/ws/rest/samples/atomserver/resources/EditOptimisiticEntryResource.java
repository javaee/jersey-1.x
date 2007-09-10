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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EditOptimisiticEntryResource extends EditEntryResource {
    private int version;
    String editURI;
    
    public EditOptimisiticEntryResource(String entryId, int version, UriInfo uriInfo) throws FeedException {
        super(entryId, uriInfo);
        
        // Get the edit link in the entry
        Feed f = AtomStore.getFeedDocument();
        Entry e = AtomStore.findEntry(entryId, f);        
        String editLink = AtomStore.getLink(e, "edit");
        
        // Compare against the requested link
        editURI = uriInfo.getAbsolute().toString();
        boolean conflict = !editURI.startsWith(editLink);        
        if (conflict) {
            // Response with 409 Conflict
            Response r = Response.Builder.noContent().status(409).build();
            throw new WebApplicationException(r);
        }

        // Increment the version and update the edit URI
        int newVersion = version + 1;
        editURI = uriInfo.getAbsolute().toString();
        editURI = editURI.replaceFirst("/" + version + "/", "/" + newVersion + "/");
    }    
    
    protected String getEditURI() {
        return editURI;
    }
}

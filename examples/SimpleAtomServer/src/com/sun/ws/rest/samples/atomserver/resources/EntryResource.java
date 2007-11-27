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

import java.io.InputStream;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@ConsumeMime("application/atom+xml")
@ProduceMime("application/atom+xml")
public class EntryResource {
    protected String entryId;
    protected String entryPath;
    
    public EntryResource(String entryId) {
        this.entryId = entryId;
        
        // Check that the entry exists, , otherwise 404
        this.entryPath = AtomStore.getEntryPath(entryId);
        AtomStore.checkExistence(entryPath);
    }
    
    @HttpMethod
    public InputStream getEntry() {
        return FileStore.FS.getFileContents(entryPath);
    }    
    
    @HttpMethod
    @Path("media")
    @ProduceMime("*/*")
    public Response getMedia() {
        // Check that the media exists, otherwise 404
        String mediaPath = AtomStore.getMediaPath(entryId);
        AtomStore.checkExistence(mediaPath);

        InputStream in = FileStore.FS.getFileContents(mediaPath);

        // TODO set the content type
        return Response.Builder.representation(in).build();
    }        
}

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

package com.sun.ws.rest.samples.servlet.resources;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/resource4")
@ConsumeMime("text/plain")
public class ResourceBean4 {
    
    public ResourceBean4() {
    }
    
    @GET
    @ProduceMime("text/html")
    public String getAsHtml() {
        return "<html><head></head><body><p>Hello World</p></body></html>";
    }

    @GET
    @ProduceMime("application/xml")
    public String getAsXml() {
        return "<response>Hello World</response>";
    }
    
    @GET
    @ProduceMime("text/plain")
    public String getAsText() {
        return "Hello World";
    }
    
    @GET
    @ProduceMime("*/*")
    public Response get(@QueryParam("format") String format) {
        return Response.ok("Hello World", format).build();
    }
    
    @POST
    @ProduceMime("text/plain")
    public String postText(String input) {
        return input;
    }
    
}

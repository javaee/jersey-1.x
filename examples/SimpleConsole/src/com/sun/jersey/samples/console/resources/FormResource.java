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

package com.sun.jersey.samples.console.resources;

import com.sun.jersey.api.representation.Form;
import java.io.InputStream;
import java.util.Date;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * A Web form resource, produces the form and processes the results of
 * submitting it.
 * 
 */
@Path("/form")
@ProduceMime("text/html")
public class FormResource {
    
    private static final Colours coloursResource = new Colours();
    
    @Context
    HttpHeaders headers;
    
    @Path("colours")
    public Colours getColours() {
        return coloursResource;
    }
    
    /**
     * Produce a form from a static HTML file packaged with the compiled class
     * @return a stream from which the HTML form can be read.
     */
    @GET
    public Response getForm() {
        Date now = new Date();

        InputStream entity = this.getClass().getResourceAsStream("form.html");
        return Response.ok(entity).
                cookie(new NewCookie("date",now.toString())).build();
    }
    
    /**
     * Process the form submission. Produces a table showing the form field
     * values submitted.
     * @return a dynamically generated HTML table.
     * @param formData the data from the form submission
     */
    @POST
    @ConsumeMime("application/x-www-form-urlencoded")
    public String processForm(Form formData) {
        StringBuffer buf = new StringBuffer();
        buf.append("<html><head><title>Form results</title></head><body>");
        buf.append("<p>Hello, you entered the following information: </p><table border='1'>");
        for (String key: formData.keySet()) {
            if (key.equals("submit"))
                continue;
            buf.append("<tr><td>");
            buf.append(key);
            buf.append("</td><td>");
            buf.append(formData.getFirst(key));
            buf.append("</td></tr>");
        }
        for (Cookie c : headers.getCookies().values()) {
            buf.append("<tr><td>Cookie: ");
            buf.append(c.getName());
            buf.append("</td><td>");
            buf.append(c.getValue());
            buf.append("</td></tr>");
        }
        
        buf.append("</table></body></html>");
        return buf.toString();
    }
    
}

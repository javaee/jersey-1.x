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

package com.sun.ws.rest.impl.container.servlet;

import javax.ws.rs.Path;
import com.sun.ws.rest.api.core.HttpContext;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.representation.Form;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

@Path("/test2/{arg1}/{arg2}")
public class MyWebResourceBean  {
    @Context HttpContext context;
    
    int count =0;
    
    @POST
    public void doPost() {
        System.out.println("MyWebResourceBean POST: " + 
                context.getRequest().getHttpMethod());
    }
    
    @GET
    public Object doGet() {
        HttpRequestContext request = context.getRequest();
        System.out.println("MyWebResourceBean GET: "+ request.getHttpMethod());
        
        List<String> reps = context.getUriInfo().getQueryParameters().get("rep");
        MultivaluedMap<String, String> params = context.getUriInfo().getTemplateParameters();
        String arg1 = params.getFirst("arg1");
        String arg2 = params.getFirst("arg2");
        
        int rep = 0;
        if (reps != null && reps.size() > 0)
            rep = Integer.parseInt((reps.get(0)));
        
        String msg = "Received args: arg1: "+arg1+" arg2: "+arg2+"\n"
                    +"Please specify a \"rep\" queryParameter using one of the following values\n"
                    +"For example, http://localhost:/rest/test2/arg1/arg2?rep=1\n"
                    +"Valid Representations:\n"
                    +"\t0 - StringRepresentation of this message\n"
                    +"\t1 - StringRepresentation\n"
                    +"\t2 - FormURLEncodedRepresentation\n";
        
        Object representation = null;
        switch (rep) {
            case 0:
                representation = msg;
                break;
            case 1:
                representation = "representation: StringRepresentation: arg1: "
                        +arg1+" arg2: "+arg2+"\n\n";
                break;
            case 2:
                Form urlProps = new Form();
                urlProps.add("representation", "FormURLEncodedRepresentation");
                urlProps.add("name", "Doug Kohlert");
                urlProps.add("sex", "male");
                urlProps.add("arg1", arg1);
                urlProps.add("arg2", arg2);
                representation = urlProps;
                break;
            case 3:
                break;
        }
        
        return representation;
    }
    
    @PUT
    public void doPut() {
        System.out.println("MyWebResourceBean PUT: " +
                context.getRequest().getHttpMethod());
    }
    
    @DELETE
    public void doDelete() {
        System.out.println("MyWebResourceBean DELETE: " +
                context.getRequest().getHttpMethod());
    }
}
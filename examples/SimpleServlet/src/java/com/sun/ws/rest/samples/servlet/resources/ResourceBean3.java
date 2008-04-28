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

import com.sun.jersey.api.representation.Form;
import java.net.URL;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/resource3/{arg1}/{arg2}")
public class ResourceBean3  {    
    @Context UriInfo uriInfo;
    
    int count =0;
    
    @POST
    public void doPost() {
        System.out.println("ResourceBean3 POST");
    }
    
    @GET
    public Response doGet() {
        MultivaluedMap<String, String> params = uriInfo.getPathParameters();
        String arg1 = params.getFirst("arg1");
        String arg2 = params.getFirst("arg2");
        for (String key : uriInfo.getQueryParameters().keySet())
            System.out.println("key: " + key + " value: " + uriInfo.getQueryParameters().getFirst(key));
        int rep = Integer.parseInt(uriInfo.getQueryParameters().getFirst("rep"));
       
        String help = "<pre>Received args: arg1: "+arg1+" arg2: "+arg2+"\n"
                    +"Please specify a \"rep\" queryParameter using one of the following values\n"
                    +"For example, http://localhost:/rest/test2/arg1/arg2?rep=1\n"
                    +"Valid Representations:\n"
                    +"\t0 - StringRepresentation of this message\n"
                    +"\t1 - StringRepresentation\n"
                    +"\t2 - FormURLEncodedRepresentation\n"
                    +"\t3 - DataSourceRepresentation\n</pre>";
        Response r = null;
        System.out.println("rep: "+rep);
        switch (rep) {
            case 0:                        
                r = Response.ok(help, "text/plain").
                        header("resource3-header", "text/plain").build();
                break;
            case 1:
                r = Response.ok(getStringRep(arg1, arg2), "text/plain").
                        header("resource3-header", "text/plain").build();
                break;
            case 2:
                r = Response.ok(getFormURLEncodedRep(arg1, arg2), "text/plain").
                        header("resource3-header", "text/plain").build();
                break;
            case 3:
                r = Response.ok(getImageRep(), "text/plain").
                        header("resource3-header", "text/plain").build();
                break;
            default :
                r = Response.ok(help, "text/plain").build();
                break;
        } 
       
        
        return r;
    }
    
    @ProduceMime("text/plain")
    @GET
    public String getStringRep(@PathParam("arg1")String arg1, 
            @PathParam("arg2")String arg2) {
        return "representation: StringRepresentation: arg1: "
                        +arg1+" arg2: "+arg2+"\n\n";
    }    
    
    @ProduceMime("application/x-www-form-urlencoded")
    @GET
    public Form getFormURLEncodedRep(
            @PathParam("arg1")String arg1, 
            @PathParam("arg2")String arg2) {
        Form urlProps = new Form();
        urlProps.add("representation", "FormURLEncodedRepresentation");
        urlProps.add("name", "Master Duke");
        urlProps.add("sex", "male");
        urlProps.add("arg1", arg1);
        urlProps.add("arg2", arg2);
        return urlProps;        
    }

    @ProduceMime("image/jpg")
    @GET
    public DataSource getImageRep() {
        URL jpgURL = this.getClass().getResource("java.jpg");
        return new FileDataSource(jpgURL.getFile());
      
    }    
}
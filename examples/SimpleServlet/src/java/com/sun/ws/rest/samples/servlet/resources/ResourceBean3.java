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

import com.sun.ws.rest.api.representation.FormURLEncodedProperties;
import java.net.URL;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@UriTemplate("/resource3/{arg1}/{arg2}")
public class ResourceBean3  {    
    @HttpContext UriInfo uriInfo;
    
    int count =0;
    
    @HttpMethod("POST")
    public void doPost() {
        System.out.println("ResourceBean3 POST");
    }
    
    @HttpMethod("GET")
    public Response doGet() {
        MultivaluedMap<String, String> params = uriInfo.getURIParameters();
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
                r = Response.Builder.representation(help, "text/plain").
                        header("resource3-header", "text/plain").build();
                break;
            case 1:
                r = Response.Builder.representation(getStringRep(arg1, arg2), "text/plain").
                        header("resource3-header", "text/plain").build();
                break;
            case 2:
                r = Response.Builder.representation(getFormURLEncodedRep(arg1, arg2), "text/plain").
                        header("resource3-header", "text/plain").build();
                break;
            case 3:
                r = Response.Builder.representation(getImageRep(), "text/plain").
                        header("resource3-header", "text/plain").build();
                break;
            default :
                r = Response.Builder.representation(help, "text/plain").build();
                break;
        } 
       
        
        return r;
    }
    
    @ProduceMime("text/plain")
    @HttpMethod("GET")
    public String getStringRep(@UriParam("arg1")String arg1, 
            @UriParam("arg2")String arg2) {
        return "representation: StringRepresentation: arg1: "
                        +arg1+" arg2: "+arg2+"\n\n";
    }    
    
    @ProduceMime("application/x-www-form-urlencoded")
    @HttpMethod("GET")
    public FormURLEncodedProperties  getFormURLEncodedRep(
            @UriParam("arg1")String arg1, 
            @UriParam("arg2")String arg2) {
        FormURLEncodedProperties urlProps = new FormURLEncodedProperties();
        urlProps.put("representation", "FormURLEncodedRepresentation");
        urlProps.put("name", "Master Duke");
        urlProps.put("sex", "male");
        urlProps.put("arg1", arg1);
        urlProps.put("arg2", arg2);
        return urlProps;        
    }

    @ProduceMime("image/jpg")
    @HttpMethod("GET")
    public DataSource getImageRep() {
        URL jpgURL = this.getClass().getResource("java.jpg");
        return new FileDataSource(jpgURL.getFile());
      
    }    
}
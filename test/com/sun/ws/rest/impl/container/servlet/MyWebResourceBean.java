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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;
import com.sun.ws.rest.api.core.HttpContextAccess;
import com.sun.ws.rest.api.core.HttpRequestContext;
import com.sun.ws.rest.api.representation.FormURLEncodedProperties;
import java.util.List;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.MultivaluedMap;

@UriTemplate("/test2/{arg1}/{arg2}")
public class MyWebResourceBean  {
    @HttpContext HttpContextAccess context;
    
    int count =0;
    
    @HttpMethod("POST")
    public void doPost() {
        System.out.println("MyWebResourceBean POST: " + 
                context.getHttpRequestContext().getHttpMethod());
    }
    
    @HttpMethod("GET")
    public Object doGet() {
        HttpRequestContext request = context.getHttpRequestContext();
        System.out.println("MyWebResourceBean GET: "+ request.getHttpMethod());
        
        List<String> reps = request.getQueryParameters().get("rep");
        MultivaluedMap<String, String> params = request.getURIParameters();
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
                FormURLEncodedProperties urlProps = new FormURLEncodedProperties();
                urlProps.put("representation", "FormURLEncodedRepresentation");
                urlProps.put("name", "Doug Kohlert");
                urlProps.put("sex", "male");
                urlProps.put("arg1", arg1);
                urlProps.put("arg2", arg2);
                representation = urlProps;
                break;
            case 3:
                break;
        }
        
        return representation;
    }
    
    @HttpMethod("PUT")
    public void doPut() {
        System.out.println("MyWebResourceBean PUT: " +
                context.getHttpRequestContext().getHttpMethod());
    }
    
    @HttpMethod("DELETE")
    public void doDelete() {
        System.out.println("MyWebResourceBean DELETE: " +
                context.getHttpRequestContext().getHttpMethod());
    }
}
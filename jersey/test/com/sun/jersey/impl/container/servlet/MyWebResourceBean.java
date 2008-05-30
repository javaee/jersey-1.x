/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.container.servlet;

import javax.ws.rs.Path;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.representation.Form;
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
        MultivaluedMap<String, String> params = context.getUriInfo().getPathParameters();
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
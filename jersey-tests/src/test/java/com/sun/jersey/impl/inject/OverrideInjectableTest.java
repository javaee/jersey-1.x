/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.impl.inject;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;

public class OverrideInjectableTest extends AbstractResourceTester {
    
    public OverrideInjectableTest(String testName) {
        super( testName );
    }
    
    @Provider
    public static class QueryParamInjectableProvider implements 
            InjectableProvider<QueryParam, Parameter> {

        private final @Context HttpContext hc;

        public QueryParamInjectableProvider(@Context HttpContext hc) {
            this.hc = hc;
        }
        
        public ComponentScope getScope() {
            return ComponentScope.PerRequest;
        }
        
        public Injectable<Map<String, String>> getInjectable(ComponentContext ic, 
                QueryParam a, Parameter c) {
            if (Map.class != c.getParameterClass())
                return null;
            
            final String name = c.getSourceName();
            return new Injectable<Map<String, String>>() {
                public Map<String, String> getValue() {
                    String value = hc.getUriInfo().getQueryParameters().getFirst(name);

                    Map<String, String> m = new LinkedHashMap<String, String>();
                    String[] kvs = value.split(",");
                    for (String kv : kvs) {
                        String[] nv = kv.split("=");
                        m.put(nv[0].trim(), nv[1].trim());
                    }
                    
                    return m;
                }
            };
        }
    }
        
    @Path("/")
    public static class MethodInjected {
        @GET
        public String get(@QueryParam("l") Map<String, String> kv) throws Exception {
            String v = "";
            for (Map.Entry<String, String> e : kv.entrySet())
                v += e.getKey() + e.getValue();
            return v;
        }                
    }
    
    public void testMethodInjected() throws IOException {                
        initiateWebApplication(MethodInjected.class,
                QueryParamInjectableProvider.class);
                
        URI u = UriBuilder.fromPath("").
                        queryParam("l", "1=2, 3=4, 5=6").build();
        assertEquals("123456", resource("/").uri(u).get(String.class));   
    }
}
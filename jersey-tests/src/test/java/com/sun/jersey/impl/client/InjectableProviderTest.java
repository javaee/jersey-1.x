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

package com.sun.jersey.impl.client;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.impl.container.grizzly.*;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class InjectableProviderTest extends AbstractGrizzlyServerTester {
    public static class InjectableType { 
        String s;
        
        InjectableType(String s) {
            this.s = s;
        }
    }

    @Provider
    public static class InjectableTypeProvider implements InjectableProvider<Context, Type> {

        public ComponentScope getScope() {
            return ComponentScope.Singleton;
        }

        public Injectable getInjectable(ComponentContext ic, Context a, Type c) {
            if (c != InjectableType.class)
                return null;
            final InjectableType it = new InjectableType("INJECT");
            
            return new Injectable<InjectableType>() {
                public InjectableType getValue() {
                    return it;
                }
            };
        }
    }

    public static class StringType {
        String s;

        StringType(String s) {
            this.s = s;
        }
    }

    @Provider
    public static class StringTypeWriter implements MessageBodyWriter<StringType> {
        @Context InjectableType it;

        public boolean isWriteable(Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return StringType.class.isAssignableFrom(type);
        }

        public long getSize(StringType t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType) {
            return -1;
        }

        public void writeTo(StringType t, Class<?> type, Type genericType,
                Annotation[] annotations, MediaType mediaType,
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException, WebApplicationException {
            String s = it.s + " " + t.s;
            entityStream.write(s.getBytes());
        }
    }

    @Path("/")
    public static class Resource {
        @POST
        public String get(String s) {
            return s;
        }               
    }
        
    public InjectableProviderTest(String testName) {
        super(testName);
    }
    
    public void testInjectableProvider() throws Exception {
        startServer(Resource.class);

        ClientConfig cc = new DefaultClientConfig();
        cc.getClasses().add(InjectableTypeProvider.class);
        cc.getClasses().add(StringTypeWriter.class);
        Client c = Client.create(cc);
        
        WebResource r = c.resource(getUri().build());
        String s = r.post(String.class, new StringType("test"));
        assertEquals("INJECT test", s);
    }
}
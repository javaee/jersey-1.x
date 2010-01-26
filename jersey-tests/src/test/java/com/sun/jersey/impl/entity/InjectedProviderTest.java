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

package com.sun.jersey.impl.entity;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class InjectedProviderTest extends AbstractResourceTester {
    public static class Bean implements Serializable {
        private String string;

        public Bean() { }

        public Bean(String string) {
            this.string = string;
        }

        public String getString() { return string; }

        public void setString(String string) { this.string = string; }
    }

    @Provider
    public static class InjectedBeanProvider extends AbstractMessageReaderWriterProvider<Bean> {
        @Context UriInfo uriInfo;
        
        public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return type == Bean.class;
        }

        public Bean readFrom(
                Class<Bean> type, 
                Type genericType, 
                Annotation annotations[],
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders, 
                InputStream entityStream) throws IOException {
            ObjectInputStream oin = new ObjectInputStream(entityStream);
            try {
                return (Bean)oin.readObject();
            } catch (ClassNotFoundException cause) {
                IOException effect = new IOException(cause.getLocalizedMessage());
                effect.initCause(cause);
                throw effect;
            }
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
            return type == Bean.class;
        }
    
        public void writeTo(
                Bean t, 
                Class<?> type, 
                Type genericType, 
                Annotation annotations[], 
                MediaType mediaType, 
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {
            t.setString(uriInfo.getRequestUri().toString());
            ObjectOutputStream out = new ObjectOutputStream(entityStream);
            out.writeObject(t);
            out.flush();
        }
    }
    
    @Path("/one/two/three")
    public static class BeanResource {
        @GET
        public Bean get() {
            return new Bean("");
        }
    }
    
    public InjectedProviderTest(String testName) {
        super(testName);
    }
    
    public void testBean() throws Exception {
        initiateWebApplication(BeanResource.class, InjectedBeanProvider.class);
                
        WebResource r = resource("/one/two/three");
        Bean b = r.get(Bean.class);
        String requestUri = UriBuilder.fromUri(BASE_URI).
                path(BeanResource.class).build().toString();
        assertEquals(requestUri, b.getString());
    }    
}

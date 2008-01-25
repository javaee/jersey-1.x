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

package com.sun.ws.rest.impl.entity;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.impl.client.ResourceProxy;
import com.sun.ws.rest.impl.provider.entity.AbstractTypeEntityProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class BeanStreamingTest extends AbstractResourceTester {
    public static class Bean implements Serializable {
        private String string;
        private int integer;
        private float real;

        public Bean() { }

        public Bean(String string, int integer, float real) {
            this.string = string;
            this.integer = integer;
            this.real = real;
        }

        public String getString() { return string; }

        public void setString(String string) { this.string = string; }

        public int getInteger() { return integer; }

        public void setInteger(int integer) { this.integer = integer; }

        public float getReal() { return real; }

        public void setReal(float real) { this.real = real; }    
    }

    @Provider
    @ProduceMime("application/bean")
    @ConsumeMime("application/bean")
    public static class BeanProvider extends AbstractTypeEntityProvider<Bean> {

        public boolean supports(Class type) {
            return type == Bean.class;
        }

        public Bean readFrom(Class<Bean> type, MediaType mediaType, 
                MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException {
            ObjectInputStream oin = new ObjectInputStream(entityStream);
            try {
                return (Bean)oin.readObject();
            } catch (ClassNotFoundException cause) {
                IOException effect = new IOException(cause.getLocalizedMessage());
                effect.initCause(cause);
                throw effect;
            }
        }

        public void writeTo(Bean t, MediaType mediaType,
                MultivaluedMap<String, Object> headers, OutputStream entityStream) throws IOException {
            ObjectOutputStream out = new ObjectOutputStream(entityStream);
            out.writeObject(t);
            out.flush();
        }
    }
    
    @Provider
    @ProduceMime("application/*")
    @ConsumeMime("application/*")
    public static class BeanWildProvider extends BeanProvider {
        @Override
        public boolean supports(Class type) {
            return type == Bean.class;
        }
    }
    
    @Provider
    @ProduceMime("application/bean")
    @ConsumeMime("application/bean")
    public static class Bean2Provider extends AbstractTypeEntityProvider<Bean2> {

        public boolean supports(Class type) {
            return type == Bean2.class;
        }

        public Bean2 readFrom(Class<Bean2> type, MediaType mediaType, 
                MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException {
            ObjectInputStream oin = new ObjectInputStream(entityStream);
            try {
                return (Bean2)oin.readObject();
            } catch (ClassNotFoundException cause) {
                IOException effect = new IOException(cause.getLocalizedMessage());
                effect.initCause(cause);
                throw effect;
            }
        }

        public void writeTo(Bean2 t, MediaType mediaType,
                MultivaluedMap<String, Object> headers, OutputStream entityStream) throws IOException {
            ObjectOutputStream out = new ObjectOutputStream(entityStream);
            out.writeObject(t);
            out.flush();
        }
    }
    
    @Provider
    @ProduceMime("application/*")
    @ConsumeMime("application/*")
    public static class Bean2WildProvider extends Bean2Provider {
        @Override
        public boolean supports(Class type) {
            return type == Bean2.class;
        }
    }
    
    public static class Bean2 extends Bean {
        public Bean2() { super(); }

        public Bean2(String string, int integer, float real) {
            super(string, integer, real);
        }
    }
    
    public BeanStreamingTest(String testName) {
        super(testName);
    }
    
    
    
    @Path("/bean")
    public static class BeanResource {
        @POST
        @ConsumeMime("application/bean")
        @ProduceMime("application/bean")
        public Bean post(Bean t) {
            return t;
        }
    }
    
    @Path("/bean")
    public static class Bean2Resource {
        @POST
        @ConsumeMime("application/bean")
        @ProduceMime("application/bean")
        public Bean2 post(Bean2 t) {
            return t;
        }
    }
    
    @Path("/plain")
    public static class BeanTextPlainResource {
        @POST
        @ConsumeMime("text/plain")
        @ProduceMime("text/plain")
        public Bean post(Bean t) {
            return t;
        }
    }
    
    @Path("/plain")
    public static class Bean2TextPlainResource {
        @POST
        @ConsumeMime("text/plain")
        @ProduceMime("text/plain")
        public Bean2 post(Bean2 t) {
            return t;
        }
    }
    
    @Path("/wild")
    public static class BeanWildResource {
        @POST
        @ConsumeMime("application/*")
        @ProduceMime("application/*")
        public Bean post(Bean t) {
            return t;
        }
    }
    
    @Path("/wild")
    public static class Bean2WildResource {
        @POST
        @ConsumeMime("application/*")
        @ProduceMime("application/*")
        public Bean2 post(Bean2 t) {
            return t;
        }
    }
    
    public void testBean() throws Exception {
        initiateWebApplication(
                BeanProvider.class, BeanWildProvider.class, 
                Bean2Provider.class,
                Bean2WildProvider.class, 
                BeanResource.class, BeanTextPlainResource.class);
                
        Bean b = new Bean("bean", 123, 3.1415f);        
        
        // the following should work using BeanProvider which
        // supports Bean.class for type application/bean
        ResourceProxy r = resourceProxy("/bean");
        r.content(b, "application/bean").post(Bean.class);

        try {
            r = resourceProxy("/plain");
            r.content(b, "text/plain").post(Bean.class);
            assertFalse(false);
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }
    
    public void testBeanWild() throws Exception {
        initiateWebApplication(
                BeanProvider.class, BeanWildProvider.class, 
                Bean2Provider.class,
                Bean2WildProvider.class, 
                BeanWildResource.class);
        
        Bean b = new Bean("bean", 123, 3.1415f);
        
        // the following should work using BeanWildProvider which
        // supports Bean.class for type application/*
        ResourceProxy r = resourceProxy("/wild");
        r.content(b, "application/wild-bean").post(Bean.class);
    }
    
    
    public void testBean2() throws Exception {
        initiateWebApplication(
                BeanProvider.class, BeanWildProvider.class, 
                Bean2Provider.class,
                Bean2WildProvider.class, 
                Bean2Resource.class, Bean2TextPlainResource.class);
                
        Bean2 b = new Bean2("bean", 123, 3.1415f);        
        
        ResourceProxy r = resourceProxy("/bean");
        r.content(b, "application/bean").post(Bean2.class);

        try {
            r = resourceProxy("/plain");
            r.content(b, "text/plain").post(Bean2.class);
            assertFalse(false);
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }
    
    public void testBean2UsingBean() throws Exception {
        initiateWebApplication(
                BeanProvider.class, BeanWildProvider.class, 
                Bean2Provider.class,
                Bean2WildProvider.class, 
                BeanResource.class, BeanTextPlainResource.class);
                
        Bean2 b = new Bean2("bean", 123, 3.1415f);        
        
        // the following should work using BeanProvider which
        // supports Bean.class for type application/bean
        ResourceProxy r = resourceProxy("/bean");
        r.content(b, "application/bean").post(Bean2.class);

        try {
            r = resourceProxy("/plain");
            r.content(b, "text/plain").post(Bean2.class);
            assertFalse(false);
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }

    public void testBean2Wild() throws Exception {
        initiateWebApplication(
                BeanProvider.class, BeanWildProvider.class, 
                Bean2Provider.class,
                Bean2WildProvider.class, 
                Bean2WildResource.class);
        
        Bean2 b = new Bean2("bean", 123, 3.1415f);
        
        // the following should work using BeanWildProvider which
        // supports Bean.class for type application/*
        ResourceProxy r = resourceProxy("/wild");
        r.content(b, "application/wild-bean").post(Bean2.class);
    }
    
    public void testBean2WildUsingBean() throws Exception {
        initiateWebApplication(
                BeanProvider.class, BeanWildProvider.class, 
                Bean2Provider.class,
                Bean2WildProvider.class, 
                BeanWildResource.class);
        
        Bean2 b = new Bean2("bean", 123, 3.1415f);
        
        // the following should work using BeanWildProvider which
        // supports Bean.class for type application/*
        ResourceProxy r = resourceProxy("/wild");
        r.content(b, "application/wild-bean").post(Bean2.class);
    }    
}

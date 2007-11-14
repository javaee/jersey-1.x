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

import com.sun.ws.rest.impl.provider.entity.AbstractTypeEntityProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class BeanStreamingTest extends AbstractStreamingTester {
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
    
    @ProduceMime("application/*")
    @ConsumeMime("application/*")
    public static class BeanWildProvider extends BeanProvider {
        @Override
        public boolean supports(Class type) {
            return type == Bean.class;
        }
    }
    
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
    
    public void testBean() throws Exception {
        Bean b = new Bean("bean", 123, 3.1415f);
        
        // the following should work using BeanProvider which
        // supports Bean.class for type application/bean
        roundTrip(Bean.class, b, "application/bean");
        
        try {
            // the following should fail since there's no entity
            // provider for Bean.class that supports text/plain
            roundTrip(Bean.class, b, "text/plain");
            assertFalse(false);
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }
    
    public void testBeanWild() throws Exception {
        Bean b = new Bean("bean", 123, 3.1415f);
        
        // the following should work using BeanWildProvider which
        // supports Bean.class for type application/*
        roundTrip(Bean.class, b, "application/wild-bean");
    }
    
    public void testBean2() throws Exception {
        Bean2 b = new Bean2("bean", 123, 3.1415f);
        
        // the following should work using BeanProvider which
        // supports Bean.class for type application/bean
        roundTrip(Bean2.class, b, "application/bean");
        
        try {
            // the following should fail since there's no entity
            // provider for Bean.class that supports text/plain
            roundTrip(Bean2.class, b, "text/plain");
            assertFalse(false);
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
        }
    }
    
    public void testBean2Wild() throws Exception {
        Bean2 b = new Bean2("bean", 123, 3.1415f);
        
        // the following should work using BeanWildProvider which
        // supports Bean.class for type application/*
        roundTrip(Bean2.class, b, "application/wild-bean2");
    }
}

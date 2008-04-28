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
package com.sun.ws.rest.impl.inject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.jersey.spi.resource.Inject;
import com.sun.jersey.spi.service.ComponentProvider;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Apr 12, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class InjectAnnotationInjectableTest extends AbstractResourceTester {
    
    public InjectAnnotationInjectableTest(String testName) {
        super( testName );
    }

    @Path("/")
    public static class MyResource {
        
        @Inject MyBean myBean;
        
        @GET
        public MyBean get() {
            assertNotNull( myBean );
            return myBean;
        }                
    }
    
    public void testInjected() throws IOException {
        final String value = "foo";
        
        initiateWebApplication( new MyComponentProvider( value ), MyResource.class);

        final MyBean myBean = resource("/").get(MyBean.class);
        assertNotNull( myBean );
        assertEquals( value, myBean.value );        
    }
    
    static class MyComponentProvider implements ComponentProvider {
        
        private final String _valueToSet;

        public MyComponentProvider( String valueToSet ) {
            _valueToSet = valueToSet;
        }

        public <T> T getInjectableInstance( T instance ) {
            return instance;
        }

        public <T> T getInstance( Scope scope, Class<T> c ) throws InstantiationException, IllegalAccessException {
            if ( MyBean.class.equals( c ) ) {
                return c.cast( new MyBean( _valueToSet ) );
            }
            return null;
        }

        public <T> T getInstance( Scope scope, Constructor<T> constructor, Object[] parameters ) throws InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return getInstance( scope, constructor.getDeclaringClass() );
        }

        public void inject( Object instance ) {
        }
        
    }

}

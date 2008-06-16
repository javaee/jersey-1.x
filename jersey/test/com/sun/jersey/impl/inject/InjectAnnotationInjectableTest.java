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
package com.sun.jersey.impl.inject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.inject.Inject;
import com.sun.jersey.spi.service.ComponentContext;
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
            return null;
        }

        public <T> T getInstance( Scope scope, Constructor<T> constructor, Object[] parameters ) throws InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return getInstance( scope, constructor.getDeclaringClass() );
        }

        public <T> T getInstance( ComponentContext cc, Scope scope, Class<T> c ) throws InstantiationException, IllegalAccessException {
            if ( MyBean.class.equals( c ) ) {                
                return c.cast( new MyBean( _valueToSet ) );
            }
            return null;
        }
        
        public void inject( Object instance ) {
        }   
    }
}
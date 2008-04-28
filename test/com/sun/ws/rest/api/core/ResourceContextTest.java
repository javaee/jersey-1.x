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
package com.sun.ws.rest.api.core;

import com.sun.jersey.api.core.ResourceContext;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.xml.bind.annotation.XmlRootElement;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.jersey.spi.service.ComponentProvider;

/**
 * Test {@link ResourceContext}: resource context must provide access to
 * subresources that can be provided by a custom component provider.<br>
 * Created on: Apr 19, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class ResourceContextTest extends AbstractResourceTester {

    public ResourceContextTest(String testName) {
        super( testName );
    }

    @Path("/")
    public static class MyRootResource {
        
        @Context ResourceContext _resourceContext;
        
        @Path( "subresource" )
        public MySubResource getMySubResource() {
            MySubResource result = _resourceContext.getResource( MySubResource.class );
            return result;
        }      
        
    }

    public static class MySubResource {
        
        private final MyBean _myBean;
        
        public MySubResource( MyBean myBean ) {
            _myBean = myBean;
        }
        
        @GET
        public MyBean get() {
            return _myBean;
        }                
    }
    
    public void testGetResourceFromResourceContext() throws IOException {
        
        final String value = "foo";
        final MyBean expected = new MyBean( value );
        
        final MySubResource mySubResource = new MySubResource( expected );
        
        initiateWebApplication( new MyComponentProvider( mySubResource ), MyRootResource.class );

        final MyBean actual = resource("/subresource").get( MyBean.class );
        assertNotNull( actual );
        assertEquals( expected, actual );        
    }
    
    static class MyComponentProvider implements ComponentProvider {
        
        private final MySubResource _subResourceToProvide;

        public MyComponentProvider( MySubResource mySubResource ) {
            _subResourceToProvide = mySubResource;
        }

        public <T> T getInjectableInstance( T instance ) {
            return instance;
        }

        public <T> T getInstance( Scope scope, Class<T> c ) throws InstantiationException, IllegalAccessException {
            if ( MySubResource.class.equals( c ) ) {
                return c.cast( _subResourceToProvide );
            }
            return null;
        }

        public <T> T getInstance( Scope scope, Constructor<T> constructor, Object[] parameters ) throws InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return getInstance( scope, constructor.getDeclaringClass() );
        }

        public void inject( Object instance ) {
        }
        
    }
    
    @XmlRootElement
    static class MyBean {
        
        public String value; 
        public MyBean() {}
        public MyBean(String str) {
            value = str;
        }
        
        public boolean equals(Object o) {
            if (!(o instanceof MyBean)) 
                return false;
            return ((MyBean) o).value.equals(value);
        }
        
        public String toString() {
            return "JAXBClass: "+value;
        }
    }

}

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

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.spi.resource.AnnotationInjectable;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Apr 12, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class AnnotationInjectableTest extends AbstractResourceTester {

    
    public AnnotationInjectableTest(String testName) {
        super( testName );
    }

    @Path("/")
    public static class MyResource {
        
        @MyAnnotation String injectedValue;
        
        @GET
        public String get() {
            return injectedValue;
        }                
    }
    
    public void testInjected() throws IOException {
        
        final String value = "foo";
        
        initiateWebApplication(MyResource.class);
        super.w.addInjectable( new AnnotationInjectable<MyAnnotation>() {

            @Override
            public Object getInjectableValue( Object o, Field f ) {
                return value;
            }

            @Override
            public Class<MyAnnotation> getAnnotationClass() {
                return MyAnnotation.class;
            }
            
        });
        
        assertEquals( value, resource("/").get(String.class) );   
    }
    
    @Target({FIELD, PARAMETER, CONSTRUCTOR })
    @Retention(RUNTIME)
    @Documented
    public static @interface MyAnnotation {

    }
    
}

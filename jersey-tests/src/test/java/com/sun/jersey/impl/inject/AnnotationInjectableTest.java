/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import java.lang.reflect.Type;

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

    @Target({FIELD, PARAMETER, CONSTRUCTOR })
    @Retention(RUNTIME)
    @Documented
    public static @interface MyAnnotation {
    }
    
    public static class MyAnnotationInjectableProvider implements 
            InjectableProvider<MyAnnotation, Type> {        
        final String value;
        
        public MyAnnotationInjectableProvider(String value) {
            this.value = value;
        }
        
        public ComponentScope getScope() {
            return ComponentScope.Singleton;
        }
        
        public Injectable<String> getInjectable(ComponentContext ic, MyAnnotation a, Type c) {
            return new Injectable<String>() {
                public String getValue() {
                    return value;
                }                    
            };
        }
    }
    
    @Path("/")
    public static class FieldInjected {
        @MyAnnotation String injectedValue;
        
        @GET
        public String get() {
            return injectedValue;
        }                
    }
    
    @Override
    protected void initiate(ResourceConfig c, WebApplication a) {
        c.getSingletons().add(new MyAnnotationInjectableProvider("foo"));        
    }
    
    public void testFieldInjected() throws IOException {                
        initiateWebApplication(FieldInjected.class);
        
        assertEquals("foo", resource("/").get(String.class));   
    }
    
    @Path("/")
    public static class MethodInjected {
        @GET
        public String get(@MyAnnotation String injectedValue) {
            return injectedValue;
        }                
    }
    
    public void testMethodInjected() throws IOException {                
        initiateWebApplication(MethodInjected.class);
                
        assertEquals("foo", resource("/").get(String.class));   
    }
    
    @Path("/")
    public static class ConstructorInjected {
        String injectedValue;
        
        public ConstructorInjected(@MyAnnotation String injectedValue) {
            this.injectedValue = injectedValue;
        }
        
        @GET
        public String get() {
            return injectedValue;
        }                
    }
    
    public void testConstructorInjected() throws IOException {                
        initiateWebApplication(ConstructorInjected.class);
                
        assertEquals("foo", resource("/").get(String.class));   
    }
}

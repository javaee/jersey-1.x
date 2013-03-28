/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.impl.model.parameter;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import javax.ws.rs.PathParam;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Checks that Parameters work fine with multiple annotations
 */
public class ParameterWithMultipleAnnotationsTest {

    private static Method createParameterMethod;

    @BeforeClass
    public static void beforeClass() {
        // its a private method so lets use reflection to invoke it
        createParameterMethod = null;
        Method[] methods = IntrospectionModeller.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("createParameter")) {
                createParameterMethod = method;
            }
        }
        assertNotNull("Should have found the createParameter() method on IntrospectionModeller", createParameterMethod);
    }

    @Test
    public void testParametersWithMultiple() throws Exception {
        checkMyResourceMethod("processTrailingUnknown");
        checkMyResourceMethod("processLeadingUnknown");
        checkMyResourceMethod("processLeadingAndTrailingUnknown");
        checkMyResourceMethod("processSingleUnknown");
        checkMyResourceMethod("processDoubleUnknown");
    }

    private void checkMyResourceMethod(final String methodName) throws Exception {
        final Method processMethod = MyResource.class.getDeclaredMethod(methodName, String.class);
        boolean isEncoded = false;
        Class<?>[] parameterTypes = processMethod.getParameterTypes();
        Annotation[][] parameterAnnotations = processMethod.getParameterAnnotations();
        int idx = 0;
        createParameterMethod.setAccessible(true);
        Object value = createParameterMethod.invoke(null, MyResource.class, MyResource.class, isEncoded, String.class,
                parameterTypes[idx], parameterAnnotations[idx]);
        assertTrue("Should return a Parameter but found " + value, value instanceof Parameter);
        Parameter parameter = (Parameter) value;
        assertEquals("correct", parameter.getSourceName());
    }

    @Target({
            java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.METHOD,
            java.lang.annotation.ElementType.FIELD
    })
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface LeadAnnotation {

        String value() default "lead";
    }

    @Target({
            java.lang.annotation.ElementType.PARAMETER, java.lang.annotation.ElementType.METHOD,
            java.lang.annotation.ElementType.FIELD
    })
    @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    public @interface TrailAnnotation {

        String value() default "trail";
    }

    private static class MyResource {

        public void processTrailingUnknown(@PathParam("correct") @TrailAnnotation String id) {
        }

        public void processLeadingUnknown(@LeadAnnotation @PathParam("correct") String id) {
        }

        public void processLeadingAndTrailingUnknown(@LeadAnnotation @PathParam("correct") @TrailAnnotation String id) {
        }

        public void processSingleUnknown(@LeadAnnotation("correct") String id) {
        }

        public void processDoubleUnknown(@LeadAnnotation @TrailAnnotation("correct") String id) {
        }
    }
}



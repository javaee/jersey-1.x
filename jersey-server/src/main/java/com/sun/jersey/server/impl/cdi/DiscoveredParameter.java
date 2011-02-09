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

package com.sun.jersey.server.impl.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.ws.rs.DefaultValue;

/**
 * Records all information about a JAX-RS-related parameter.
 *
 * @author robc
 */
public class DiscoveredParameter {

    private Annotation annotation;
    private Type type;
    private DefaultValue defaultValue;
    private boolean encoded;

    public DiscoveredParameter(Annotation annotation, Type type, DefaultValue defaultValue, boolean encoded) {
        this.annotation = annotation;
        this.type = type;
        this.defaultValue = defaultValue;
        this.encoded = encoded;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public Type getType() {
        return type;
    }

    public DefaultValue getDefaultValue() {
        return defaultValue;
    }

    public boolean isEncoded() {
        return encoded;
    }

    public String getValue() {
        try {
            // Annotation[] as, Annotation a, Source source, String sourceName, Type type, Class<?> clazz)
            Method valueMethod = annotation.annotationType().getDeclaredMethod("value");
            String name = (String)valueMethod.invoke(annotation);
            return name;
        }
        catch (NoSuchMethodException e) {
            return null;
        }
        catch (IllegalAccessException e) {
            // wrap and rethrow for now
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            // wrap and rethrow for now
            throw new RuntimeException(e);
        }
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (annotation == null ? 0 : annotation.hashCode());
        result = 31 * result + (type == null ? 0 : type.hashCode());
        result = 31 * result + (defaultValue == null ? 0 : defaultValue.hashCode());
        result = 31 * result + (encoded ? 7 : 11);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj.getClass() != this.getClass()) {
            return false;
        }

        DiscoveredParameter that = (DiscoveredParameter) obj;

        return (annotation == null ? that.annotation == null : annotation.equals(that.annotation)) &&
               (type == null ? that.type == null : type.equals(that.type)) &&
               (defaultValue == null ? that.defaultValue == null : defaultValue.equals(that.defaultValue)) &&
               (this.encoded == that.encoded);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DiscoveredParameter(");
        sb.append(annotation);
        sb.append(',');
        sb.append(type);
        sb.append(',');
        sb.append(defaultValue);
        sb.append(',');
        sb.append(encoded);
        sb.append(')');
        return sb.toString();
    }
}

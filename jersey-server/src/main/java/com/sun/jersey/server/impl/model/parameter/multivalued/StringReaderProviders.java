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
package com.sun.jersey.server.impl.model.parameter.multivalued;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.core.header.HttpDateFormat;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.spi.StringReader;
import com.sun.jersey.spi.StringReaderProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class StringReaderProviders {

    private static abstract class AbstractStringReader implements StringReader {

        public Object fromString(String value) {
            try {
                return _fromString(value);
            } catch (InvocationTargetException ex) {
                Throwable target = ex.getTargetException();
                if (target instanceof WebApplicationException) {
                    throw (WebApplicationException)target;
                } else {
                    throw new ExtractorContainerException(target);
                }
            } catch (Exception ex) {
                throw new ContainerException(ex);
            }
        }

        protected abstract Object _fromString(String value) throws Exception;
    }

    public static class StringConstructor implements StringReaderProvider {

        public StringReader getStringReader(Class type, Type genericType, Annotation[] annotations) {
            final Constructor constructor = ReflectionHelper.getStringConstructor(type);
            if (constructor == null)
                return null;

            return new AbstractStringReader() {
                protected Object _fromString(String value) throws Exception {
                    return constructor.newInstance(value);
                }
            };
        }
    }

    public static class TypeValueOf implements StringReaderProvider {

        public StringReader getStringReader(Class type, Type genericType, Annotation[] annotations) {
            final Method valueOf = ReflectionHelper.getValueOfStringMethod(type);
            if (valueOf == null)
                return null;

            return new AbstractStringReader() {
                public Object _fromString(String value) throws Exception {
                    return valueOf.invoke(null, value);
                }
            };
        }
    }

    public static class TypeFromString implements StringReaderProvider {

        public StringReader getStringReader(Class type, Type genericType, Annotation[] annotations) {
            final Method fromString = ReflectionHelper.getFromStringStringMethod(type);
            if (fromString == null)
                return null;

            return new AbstractStringReader() {
                public Object _fromString(String value) throws Exception {
                    return fromString.invoke(null, value);
                }
            };
        }
    }

    public static class TypeFromStringEnum extends TypeFromString {

        @Override
        public StringReader getStringReader(Class type, Type genericType, Annotation[] annotations) {
            if (!Enum.class.isAssignableFrom(type))
                return null;
            
            return super.getStringReader(type, genericType, annotations);
        }
    }

    public static class DateProvider implements StringReaderProvider {

        public StringReader getStringReader(Class type, Type genericType, Annotation[] annotations) {
            if (type != Date.class)
                return null;
            
            return new StringReader() {
                public Object fromString(String value) {
                    try {
                        return HttpDateFormat.readDate(value);
                    } catch (ParseException ex) {
                        throw new ExtractorContainerException(ex);
                    }
                }
            };
        }
    }
}

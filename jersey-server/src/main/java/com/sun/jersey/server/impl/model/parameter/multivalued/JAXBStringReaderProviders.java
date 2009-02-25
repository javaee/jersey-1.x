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
package com.sun.jersey.server.impl.model.parameter.multivalued;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.spi.StringReader;
import com.sun.jersey.spi.StringReaderProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.WeakHashMap;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class JAXBStringReaderProviders {

    private static final Map<Class, JAXBContext> jaxbContexts =
            new WeakHashMap<Class, JAXBContext>();

    private final Providers ps;

    private final ContextResolver<JAXBContext> context;

    private final ContextResolver<Unmarshaller> unmarshaller;

    private final ContextResolver<Marshaller> marshaller;

    public JAXBStringReaderProviders(Providers ps) {
        this.ps = ps;

        this.context = ps.getContextResolver(JAXBContext.class, null);
        this.unmarshaller = ps.getContextResolver(Unmarshaller.class, null);
        this.marshaller = ps.getContextResolver(Marshaller.class, null);
    }

    protected final Unmarshaller getUnmarshaller(Class type) throws JAXBException {
        if (unmarshaller != null) {
            Unmarshaller u = unmarshaller.getContext(type);
            if (u != null) {
                return u;
            }
        }

        return getJAXBContext(type).createUnmarshaller();
    }

    private final JAXBContext getJAXBContext(Class type) throws JAXBException {
        if (context != null) {
            JAXBContext c = context.getContext(type);
            if (c != null) {
                return c;
            }
        }

        return getStoredJAXBContext(type);
    }

    protected JAXBContext getStoredJAXBContext(Class type) throws JAXBException {
        synchronized (jaxbContexts) {
            JAXBContext c = jaxbContexts.get(type);
            if (c == null) {
                c = JAXBContext.newInstance(type);
                jaxbContexts.put(type, c);
            }
            return c;
        }
    }

    public static class RootElementProvider extends JAXBStringReaderProviders implements StringReaderProvider {

        public RootElementProvider(@Context Providers ps) {
            super(ps);
        }

        public StringReader getStringReader(final Class type, Type genericType, Annotation[] annotations) {
            boolean supported = (type.getAnnotation(XmlRootElement.class) != null ||
                    type.getAnnotation(XmlType.class) != null);
            if (!supported) {
                return null;
            }

            return new StringReader() {

                public Object fromString(String value) {
                    try {
                        Unmarshaller u = getUnmarshaller(type);
                        if (type.isAnnotationPresent(XmlRootElement.class)) {
                            return u.unmarshal(new java.io.StringReader(value));
                        } else {
                            return u.unmarshal(new StreamSource(new java.io.StringReader(value)), type).getValue();
                        }
                    } catch (UnmarshalException ex) {
                        throw new ContainerException(ImplMessages.ERROR_UNMARSHALLING_JAXB(type), ex);
                    } catch (JAXBException ex) {
                        throw new ContainerException(ImplMessages.ERROR_UNMARSHALLING_JAXB(type), ex);
                    }
                }
            };
        }
    }
}
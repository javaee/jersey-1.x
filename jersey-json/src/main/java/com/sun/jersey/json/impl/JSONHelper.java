/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.json.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Helper methods for JSON classes.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public final class JSONHelper {

    private static JaxbProvider jaxbProvider;

    // just to make clear no instances are meant to be created
    private JSONHelper() {
    }

    /**
     *  calculating local name of an appropriate XML element,
     *  pretty much the same way as it is done by JAXB 2.1 impl
     *  (for situations when we want to pretend the element was present
     *  in an incoming stream amd all we have is the type information)
     *  TODO: work out with JAXB guys a better way of doing it,
     *        probably we could take it from an existing JAXBContext?
     */
    public static String getRootElementName(Class<Object> clazz) {
        XmlRootElement e = clazz.getAnnotation(XmlRootElement.class);
        if (e == null) {
            return getVariableName(clazz.getSimpleName());
        }
        if ("##default".equals(e.name())) {
            return getVariableName(clazz.getSimpleName());
        } else {
            return e.name();
        }
    }

    private static String getVariableName(String baseName) {
        return NameUtil.toMixedCaseName(NameUtil.toWordList(baseName), false);
    }

    public static JaxbProvider getJaxbProvider(final JAXBContext jaxbContext) {
        for (SupportedJaxbProvider provider : SupportedJaxbProvider.values()) {
            try {
                final Class<?> jaxbContextClass = getJaxbContextClass(jaxbContext);

                Class<?> clazz = null;
                if (SupportedJaxbProvider.JAXB_JDK.equals(provider)) {
                    // We can be in OSGi runtime, so try to use system classloader.
                    clazz = ClassLoader.getSystemClassLoader().loadClass(SupportedJaxbProvider.JAXB_JDK.getJaxbContextClassName());
                } else {
                    clazz = Class.forName(provider.getJaxbContextClassName());
                }

                if (clazz.isAssignableFrom(jaxbContextClass)) {
                    return jaxbProvider = provider;
                }
            } catch (ClassNotFoundException e) {
                // Do nothing, try the next provider.
            }
        }
        throw new IllegalStateException("No JAXB provider found for the following JAXB context: "
                + (jaxbContext == null ? null : jaxbContext.getClass()));
    }

    private static Class<?> getJaxbContextClass(final JAXBContext jaxbContext) throws ClassNotFoundException {
        if (jaxbContext != null) {
            return jaxbContext.getClass();
        }

        return ClassLoader.getSystemClassLoader().loadClass(SupportedJaxbProvider.JAXB_JDK.getJaxbContextClassName());
    }

    public static boolean isNaturalNotationEnabled() {
        try {
            if (jaxbProvider == SupportedJaxbProvider.JAXB_RI) {
                Class.forName("com.sun.xml.bind.annotation.OverrideAnnotationOf");
            } else if (jaxbProvider == null || jaxbProvider == SupportedJaxbProvider.JAXB_JDK) {
                Class.forName("com.sun.xml.internal.bind.annotation.OverrideAnnotationOf");
            }

            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static Map<String, Object> createPropertiesForJaxbContext(final Map<String, Object> properties) {
        final Map<String, Object> jaxbProperties = new HashMap<String, Object>(properties.size() + 1);
        final String retainReferenceToInfo = "retainReferenceToInfo"; // JAXBContextImpl.RETAIN_REFERENCE_TO_INFO;

        jaxbProperties.putAll(properties);
        jaxbProperties.put(retainReferenceToInfo, Boolean.TRUE);

        return jaxbProperties;
    }

}

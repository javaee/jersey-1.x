/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

/**
 * Implementation of {@code JaxbXmlDocumentStructure} for JAXB provider in JDK.
 * <p/>
 * Note: If you're changing this class consider changing {@link JaxbRiXmlStructure} as well.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class JaxbJdkXmlStructure extends DefaultJaxbXmlDocumentStructure {

    private static class NodeWrapper {

        private final NodeWrapper parent;
        private final Object runtimePropertyInfo;

        private NodeWrapper(NodeWrapper parent, Object runtimePropertyInfo) {
            this.parent = parent;
            this.runtimePropertyInfo = runtimePropertyInfo;
        }

        @Override
        public int hashCode() {
            int hash = 13;
            hash += (parent == null ? 0 : parent.hashCode());
            hash += (runtimePropertyInfo == null ? 0 : runtimePropertyInfo.hashCode());
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof NodeWrapper)) {
                return false;
            }
            final NodeWrapper other = (NodeWrapper) obj;
            return runtimePropertyInfo == other.runtimePropertyInfo && parent == other.parent;
        }
    }

    private final static ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    private Map<String, QName> qNamesOfExpElems = new HashMap<String, QName>();
    private Map<String, QName> qNamesOfExpAttrs = new HashMap<String, QName>();

    private LinkedList<NodeWrapper> processedNodes = new LinkedList<NodeWrapper>();

    private final boolean isReader;

    public JaxbJdkXmlStructure(JAXBContext jaxbContext, Class<?> expectedType, boolean isReader) {
        super(jaxbContext, expectedType, isReader);
        this.isReader = isReader;
    }

    private Collection<QName> getExpectedEntities(final String methodName) {
        try {
            final Class<?> aClass = systemClassLoader
                    .loadClass("com.sun.xml.internal.bind.v2.runtime.unmarshaller.UnmarshallingContext");

            final Object getInstance = aClass.getMethod("getInstance").invoke(null);
            final Object getCurrentExpectedElements = aClass.getMethod(methodName).invoke(getInstance);

            return (Collection<QName>) getCurrentExpectedElements;
        } catch (NullPointerException npe) {
            // TODO: need to check what could be done in JAXB in order to prevent the npe
            // thrown from com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext#1206
        } catch (Exception e) {
            // Reflection problem. Ignore that and return empty list.
        }

        // something went wrong - return empty list
        return Collections.emptyList();
    }

    @Override
    public Collection<QName> getExpectedElements() {
        return getExpectedEntities("getCurrentExpectedElements");
    }

    @Override
    public Collection<QName> getExpectedAttributes() {
        if (canHandleAttributes()) {
            return getExpectedEntities("getCurrentExpectedAttributes");
        }

        return Collections.emptyList();
    }

    @Override
    public Map<String, QName> getExpectedElementsMap() {
        final Collection<QName> expectedElements = getExpectedElements();
        if (!expectedElements.isEmpty()) {
            qNamesOfExpElems = qnameCollectionToMap(expectedElements, true);
        }
        return qNamesOfExpElems;
    }

    @Override
    public Map<String, QName> getExpectedAttributesMap() {
        final Collection<QName> expectedAttributes = getExpectedAttributes();
        if (!expectedAttributes.isEmpty()) {
            qNamesOfExpAttrs = qnameCollectionToMap(expectedAttributes, false);
        }
        return qNamesOfExpAttrs;
    }

    @Override
    public boolean canHandleAttributes() {
        return JSONHelper.isNaturalNotationEnabled();
    }

    @Override
    public Type getEntityType(QName entity, boolean isAttribute) {
        final NodeWrapper peek = processedNodes.getLast();

        Object rawType;
        try {
            Class<?> runtimeReferencePropertyInfo = systemClassLoader
                    .loadClass("com.sun.xml.internal.bind.v2.model.runtime.RuntimePropertyInfo");
            rawType = runtimeReferencePropertyInfo.getMethod("getRawType").invoke(peek.runtimePropertyInfo);
        } catch (Exception e) {
            rawType = null;
        }

        return peek.runtimePropertyInfo == null ? null : (Type) rawType;
    }

    @Override
    public void startElement(final QName name) {
        if (!isReader) {
            processedNodes.add(new NodeWrapper(
                    processedNodes.isEmpty() ? null : processedNodes.getLast(),
                    getCurrentElementRuntimePropertyInfo()));
        }
    }

    @Override
    public void handleAttribute(final QName attributeName, final String value) {
        startElement(attributeName);
    }

    private Object getCurrentElementRuntimePropertyInfo() {
        try {
            final Class<?> aClass = systemClassLoader.loadClass("com.sun.xml.internal.bind.v2.runtime.XMLSerializer");

            // XMLSerializer
            final Object xs = aClass.getMethod("getInstance").invoke(null);
            final Method getCurrentProperty = aClass.getMethod("getCurrentProperty");

            // Property
            final Object cp = (xs == null) ? null : getCurrentProperty.invoke(xs);
            final Class<?> bClass = systemClassLoader.loadClass("com.sun.xml.internal.bind.v2.runtime.property.Property");
            final Method getInfo = bClass.getMethod("getInfo");

            // RuntimePropertyInfo
            return (cp == null) ? null : getInfo.invoke(cp);
        } catch (Exception e) {
            // Reflection problem.
            return null;
        }
    }

    @Override
    public boolean isArrayCollection() {
        Object runtimePropertyInfo = isReader ? null : getCurrentElementRuntimePropertyInfo();

        if (runtimePropertyInfo == null && !processedNodes.isEmpty()) {
            final NodeWrapper peek = processedNodes.getLast();
            runtimePropertyInfo = peek.runtimePropertyInfo;
        }

        boolean isCollection = false;
        try {
            Class<?> runtimeReferencePropertyInfo = systemClassLoader
                    .loadClass("com.sun.xml.internal.bind.v2.model.runtime.RuntimePropertyInfo");
            isCollection = (Boolean) runtimeReferencePropertyInfo.getMethod("isCollection").invoke(runtimePropertyInfo);
        } catch (Exception e) {
            // Reflection problem.
            isCollection = false;
        }

        return runtimePropertyInfo != null && isCollection && !isWildcardElement(runtimePropertyInfo);
    }

    @Override
    public boolean isSameArrayCollection() {
        final int size = processedNodes.size();
        if (size >= 2) {
            final NodeWrapper last = processedNodes.getLast();
            final NodeWrapper beforeLast = processedNodes.get(size - 2);

            if (last.equals(beforeLast)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasSubElements() {
        if (isReader) {
            return !getExpectedElements().isEmpty();
        } else {
            return !processedNodes.isEmpty()
                    && processedNodes.getLast() != getCurrentElementRuntimePropertyInfo();
        }
    }

    @Override
    public void endElement(QName name) {
        if (!isReader) {
            processedNodes.removeLast();
        }
    }

    private boolean isWildcardElement(Object ri) {
        try {
            Class<?> runtimeReferencePropertyInfo = systemClassLoader
                    .loadClass("com.sun.xml.internal.bind.v2.model.runtime.RuntimeReferencePropertyInfo");
            return runtimeReferencePropertyInfo.getMethod("getWildcard").invoke(ri) != null;
        } catch (Exception e) {
            return false;
        }
    }

}

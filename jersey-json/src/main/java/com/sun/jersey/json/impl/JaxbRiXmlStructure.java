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

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import com.sun.xml.bind.v2.model.runtime.RuntimePropertyInfo;
import com.sun.xml.bind.v2.model.runtime.RuntimeReferencePropertyInfo;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.property.Property;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext;

/**
 * Implementation of {@code JaxbXmlDocumentStructure} for JAXB RI provider.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class JaxbRiXmlStructure extends DefaultJaxbXmlDocumentStructure {

    private static class NodeWrapper {

        private RuntimePropertyInfo runtimePropertyInfo;

        private NodeWrapper(RuntimePropertyInfo runtimePropertyInfo) {
            this.runtimePropertyInfo = runtimePropertyInfo;
        }

    }

    /**
     * Determines whether it is possible to handle attributes with the current version of JAXB RI.
     */
    private boolean properJAXBVersion = true;

    private Map<String, QName> qNamesOfExpElems = new HashMap<String, QName>();
    private Map<String, QName> qNamesOfExpAttrs = new HashMap<String, QName>();

    private List<NodeWrapper> processedNodes = new LinkedList<NodeWrapper>();

    private final boolean isReader;

    public JaxbRiXmlStructure(boolean isReader) {
        this.isReader = isReader;
    }

    @Override
    public Collection<QName> getExpectedElements() {
        try {
            return UnmarshallingContext.getInstance().getCurrentExpectedElements();
        }  catch (NullPointerException npe) {
            // TODO: need to check what could be done in JAXB in order to prevent the npe
            // thrown from com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext#1206
        }

        // something went wrong - return empty list
        return Collections.emptyList();
    }

    @Override
    public Collection<QName> getExpectedAttributes() {
        if (properJAXBVersion) {
            try {
                return UnmarshallingContext.getInstance().getCurrentExpectedAttributes();
            } catch (NullPointerException npe) {
                // thrown from com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallingContext
            } catch (NoSuchMethodError nsme) {
                // thrown when JAXB version is less than 2.1.12
                properJAXBVersion = false;
                Logger.getLogger(getClass().getName())
                        .log(Level.SEVERE, com.sun.jersey.json.impl.ImplMessages.ERROR_JAXB_RI_2_1_12_MISSING(), nsme);
            }
        }

        // something went wrong - return empty list
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
        return properJAXBVersion;
    }

    @Override
    public Type getEntityType(QName entity, boolean isAttribute) {
        final NodeWrapper peek = processedNodes.get(processedNodes.size() - 1);
        return peek.runtimePropertyInfo == null ? null : peek.runtimePropertyInfo.getRawType();
    }

    @Override
    public void startElement(QName name) {
        if (!isReader) {
            final XMLSerializer xs = XMLSerializer.getInstance();
            final Property cp = (xs == null) ? null : xs.getCurrentProperty();
            final RuntimePropertyInfo ri = (cp == null) ? null : cp.getInfo();

            processedNodes.add(new NodeWrapper(ri));
        }
    }

    @Override
    public boolean isArrayCollection() {
        final NodeWrapper peek = processedNodes.get(processedNodes.size() - 1);
        return peek.runtimePropertyInfo != null && peek.runtimePropertyInfo.isCollection() && !isWildcardElement(peek.runtimePropertyInfo);
    }

    @Override
    public boolean isSameArrayCollection() {
        final int size = processedNodes.size();
        if (size >= 2) {
            if (processedNodes.get(size - 1).runtimePropertyInfo == processedNodes.get(size - 2).runtimePropertyInfo) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void endElement(QName name) {
        if (!isReader) {
            processedNodes.remove(processedNodes.size() - 1);
        }
    }

    private boolean isWildcardElement(RuntimePropertyInfo ri) {
        return (ri instanceof RuntimeReferencePropertyInfo) && (((RuntimeReferencePropertyInfo)ri).getWildcard() != null);
    }

}

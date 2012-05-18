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
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Default implementation of {@code JaxbXmlDocumentStructure}.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
abstract class DefaultJaxbXmlDocumentStructure implements JaxbXmlDocumentStructure {

    @Override
    public Collection<QName> getExpectedElements() {
        return Collections.emptyList();
    }

    @Override
    public Collection<QName> getExpectedAttributes() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, QName> getExpectedElementsMap() {
        return qnameCollectionToMap(getExpectedElements(), true);
    }

    @Override
    public Map<String, QName> getExpectedAttributesMap() {
        return qnameCollectionToMap(getExpectedAttributes(), false);
    }

    @Override
    public void startElement(QName name) {
    }

    @Override
    public void endElement(QName name) {
    }

    @Override
    public boolean canHandleAttributes() {
        return true;
    }

    @Override
    public Type getEntityType(QName entity, boolean isAttribute) {
        return null;
    }

    @Override
    public boolean isArrayCollection() {
        return false;
    }

    @Override
    public boolean isSameArrayCollection() {
        return true;
    }

    /**
     * Transforms a collection of qualified names into a map. Keys of this map are represented by local names and values of
     * this map are the qualified names themselves.
     *
     * @param collection collection to be transformed.
     * @param elementCollection flag whether the collection represents a collection of qualified names of elements.
     * @return map of qualified names.
     */
    protected Map<String, QName> qnameCollectionToMap(final Collection<QName> collection, final boolean elementCollection) {
        final Map<String, QName> map = new HashMap<String, QName>();

        for (QName qname : collection) {
            final String namespaceUri = qname.getNamespaceURI();

            if (elementCollection && "\u0000".equals(namespaceUri)) {
                map.put("$", null);
            } else {
                map.put(qname.getLocalPart(), qname);
            }
        }

        return map;
    }

}

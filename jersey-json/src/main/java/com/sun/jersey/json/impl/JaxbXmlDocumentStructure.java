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
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Provides XML structure information (expected elements/attributes) about JAXB element classes that are being unmarshalled.
 * <p/>
 * Implementations of this interface are notified about the change of currently processed element via methods {@link
 * #startElement(javax.xml.namespace.QName)} and {@link #endElement(javax.xml.namespace.QName)} where the {@code QName} is
 * represented by the element name.
 * <p/>
 * Note: This class is designed to support the unmarshalling JSONs in natural notation since the parser does not have other
 * means to decide whether the entities of currently processed element belongs to either to the group of elements or to the
 * group of attributes.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public interface JaxbXmlDocumentStructure {

    /**
     * Notifies this structure provider that a start element event with the given name has been fired and that this element
     * will be processed.
     *
     * @param name name of the element that will be processed.
     */
    public void startElement(final QName name);

    /**
     * Notifies this structure provider that an end element event has been fired and that the parent element of the one with
     * the given name should be processed.
     *
     * @param name name of the ending element.
     */
    public void endElement(final QName name);

    /**
     * Returns {@code true} if the implementation is capable of handling and providing information about attributes ({@code
     * #getExpectedAttributes} and {@code #getExpectedAttributesMap} return valid results), {@code false} otherwise.
     *
     * @return {@code true} if expected attributes can be obtained, {@code false} otherwise.
     */
    public boolean canHandleAttributes();

    /**
     * Notifies this structure provider about an attribute event with the given name has been fired and that this attribute
     * will be processed.
     *
     * @param attributeName name of the attribute that will be processed.
     * @param value value of the attribute.
     */
    public void handleAttribute(QName attributeName, String value);

    /**
     * Returns the {@link Type} of entity (element, attribute) with the given name. The given entity name is expected to be either
     * name of the current element or name of it's direct child.
     *
     * @param entityName name of the entity to retrieve the {@code Type} for.
     * @param isAttribute flag whether the requested entity is an attribute or not.
     * @return type of the entity or {@code null} if the entity cannot be retrieved from the structure.
     */
    public Type getEntityType(QName entityName, boolean isAttribute);

    /**
     * Returns a collection of expected attributes of currently processed element.
     *
     * @return a collection of expected attributes.
     */
    public Collection<QName> getExpectedAttributes();

    /**
     * Returns a map of expected attributes of currently processed element where {@code key} is represented by the local
     * name of the attribute and {@code value} is it's qualified name.
     *
     * @return a map of expected attributes.
     */
    public Map<String, QName> getExpectedAttributesMap();

    /**
     * Returns a collection of expected sub-elements of currently processed element.
     *
     * @return a collection of expected sub-elements.
     */
    public Collection<QName> getExpectedElements();

    /**
     * Returns a map of expected sub-elements of currently processed element where {@code key} is represented by the local
     * name of the sub-element and {@code value} is it's qualified name.
     *
     * @return a map of expected sub-elements.
     */
    public Map<String, QName> getExpectedElementsMap();

    /**
     * Returns {@code true} if the currently processed element should represent an JSON array element.
     *
     * @return {@code true} if the element is an JSON array element, {@code false} otherwise.
     */
    public boolean isArrayCollection();

    /**
     * Returns {@code true} if the currently processed element belongs to the same JSON array as the previous element.
     *
     * @return {@code true} if the element is an JSON array belongs to the same JSON array as the previous element,
     * {@code false} otherwise.
     */
    public boolean isSameArrayCollection();

    /**
     * Returns {@code true} if JAXB bean of the currently processed element can contain any subelements.
     *
     * @return {@code true} if JAXB bean of the currently processed element can contain any subelements,
     * {@code false} otherwise.
     */
    public boolean hasSubElements();

}

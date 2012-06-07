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
import java.util.Stack;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.oxm.MappingNodeValue;
import org.eclipse.persistence.internal.oxm.TreeObjectBuilder;
import org.eclipse.persistence.internal.oxm.XPathFragment;
import org.eclipse.persistence.internal.oxm.XPathNode;
import org.eclipse.persistence.jaxb.JAXBHelper;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.sessions.DatabaseSession;

/**
 * Implementation of {@code JaxbXmlDocumentStructure} for MOXy JAXB provider.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class MoxyXmlStructure extends DefaultJaxbXmlDocumentStructure {

    private final class EntityType {

        private final Type type;

        private EntityType(Type type) {
            this.type = type;
        }

    }

    /**
     * For caching purposes and because of the fact that there is no easy way to go back to the parent node ({@code getParent}
     * returns {@code null}).
     */
    private final class XPathNodeWrapper {

        private Map<String, EntityType> elementTypeMap = new HashMap<String, EntityType>();
        private Map<String, EntityType> attributeTypeMap = new HashMap<String, EntityType>();

        private Map<String, QName> qNamesOfExpElems = new HashMap<String, QName>();
        private Map<String, QName> qNamesOfExpAttrs = new HashMap<String, QName>();

        private final XPathNode xPathNode;
        private final XPathNodeWrapper parent;
        private final ClassDescriptor classDescriptor;
        private final QName name;
        private final MappingNodeValue nodeValue;

        public XPathNodeWrapper(final QName name) {
            this(null, null, null, null, name);
        }

        public XPathNodeWrapper(final XPathNode xPathNode,
                                final XPathNodeWrapper parent,
                                final MappingNodeValue nodeValue,
                                final ClassDescriptor classDescriptor,
                                final QName name) {
            this.xPathNode = xPathNode;
            this.parent = parent;
            this.nodeValue = nodeValue;
            this.classDescriptor = classDescriptor;
            this.name = name;
        }

        public Map<String, QName> getExpectedElementsMap() {
            if (qNamesOfExpElems.isEmpty()) {
                qNamesOfExpElems = qnameCollectionToMap(getExpectedElements(), true);
            }
            return qNamesOfExpElems;
        }

        public Map<String, QName> getExpectedAttributesMap() {
            if (qNamesOfExpElems.isEmpty()) {
                qNamesOfExpAttrs = qnameCollectionToMap(getExpectedAttributes(), false);
            }
            return qNamesOfExpAttrs;
        }

        public Map<String, EntityType> getEntitiesTypesMap(boolean isAttribute) {
            Map<String, EntityType> entitiesTypes = isAttribute ? attributeTypeMap : elementTypeMap;

            if (entitiesTypes.isEmpty()) {
                final Map<XPathFragment, XPathNode> nodeMap =
                        isAttribute ? xPathNode.getAttributeChildrenMap() : xPathNode.getNonAttributeChildrenMap();

                if (nodeMap != null) {
                    for(Map.Entry<XPathFragment, XPathNode> entry : nodeMap.entrySet()) {
                        entitiesTypes.put(entry.getKey().getLocalName(), new EntityType(entry.getKey().getXMLField().getType()));
                    }
                }
            }
            return entitiesTypes;
        }

        public MappingNodeValue getNodeValue() {
            return nodeValue;
        }

        public ClassDescriptor getClassDescriptor() {
            return classDescriptor;
        }

        public boolean isInSameArrayAs(final XPathNodeWrapper wrapper) {
            return wrapper != null
                    && this.classDescriptor == wrapper.classDescriptor && this.parent == wrapper.parent;
        }

    }

    /**
     * Stack of nodes which processing has started but is not finished yet.
     *
     * @see XPathNodeWrapper
     */
    private Stack<XPathNodeWrapper> xPathNodes = new Stack<XPathNodeWrapper>();

    /**
     * Last {@code XPathNodeWrapper} that was created in the {@link #startElement(javax.xml.namespace.QName)} method (can be an
     * element representing primitive value or an attribute so it may not be stored in the {@link #xPathNodes} stack).
     */
    private XPathNodeWrapper lastAccessedNode = null;

    /**
     * Expected type of unmarshalled entity. For {@code MoxyXmlStructure}.
     */
    private final Class<?> expectedType;

    /**
     * {@code JAXBContext}. For {@code MoxyXmlStructure}.
     */
    private final JAXBContext jaxbContext;

    /**
     * Flag whether the first start element event is being handled by the {@code #startElement} method. If so the method should
     * not proceed because the first element is handled in the constructor.
     */
    private boolean firstDocumentElement = true;

    private final boolean isReader;

    public MoxyXmlStructure(final JAXBContext jaxbContext, final Class<?> expectedType, final boolean isReader) {
        super(jaxbContext, expectedType, isReader);

        this.jaxbContext = jaxbContext;
        this.expectedType = expectedType;
        this.isReader = isReader;
    }

    /**
     * Creates a {@code XPathNodeWrapper} for the root element of processing XML. The XML type is derived from
     * {@code expectedType} or by using {@code elementName} if the {@code expectedType} is {@link JAXBElement}.
     *
     * @param elementName name to obtain expected type of the element if the {@code expectedType} of this class is
     * {@link JAXBElement}.
     */
    private void createRootNodeWrapperForExpectedElement(final QName elementName) {
        if (jaxbContext == null) {
            return;
        }

        final org.eclipse.persistence.jaxb.JAXBContext moxyJaxbContext = JAXBHelper.getJAXBContext(jaxbContext);
        final XMLContext xmlContext = moxyJaxbContext.getXMLContext();
        final DatabaseSession session = xmlContext.getSession(0);

        Class<?> expectedType = this.expectedType;
        if (JAXBElement.class.isAssignableFrom(expectedType)) {
            final Map<Class, ClassDescriptor> descriptors = session.getDescriptors();

            for (Map.Entry<Class, ClassDescriptor> descriptor : descriptors.entrySet()) {
                final QName defaultRootElementType = ((XMLDescriptor) descriptor.getValue()).getDefaultRootElementType();

                // Check local name.
                if (defaultRootElementType == null
                        || !defaultRootElementType.getLocalPart().contains(elementName.getLocalPart())) {
                    continue;
                }

                // Check namespace.
                if ((defaultRootElementType.getNamespaceURI() == null && elementName.getNamespaceURI() == null)
                        || (defaultRootElementType.getNamespaceURI() != null
                            && defaultRootElementType.getNamespaceURI().equals(elementName.getNamespaceURI()))) {
                    expectedType = descriptor.getKey();
                }
            }
        }

        final ClassDescriptor descriptor = session.getDescriptor(expectedType);

        if (descriptor != null) {
            final TreeObjectBuilder objectBuilder = (TreeObjectBuilder) descriptor.getObjectBuilder();

            xPathNodes.push(new XPathNodeWrapper(objectBuilder.getRootXPathNode(),
                    null, null, descriptor, new QName(expectedType.getSimpleName())));
        }
    }

    @Override
    public Collection<QName> getExpectedElements() {
        final List<QName> elements = new LinkedList<QName>();
        final XPathNodeWrapper currentNodeWrapper = getCurrentNodeWrapper();

        final Map<XPathFragment, XPathNode> nonAttributeChildrenMap =
                currentNodeWrapper == null ? null : currentNodeWrapper.xPathNode.getNonAttributeChildrenMap();

        if (nonAttributeChildrenMap != null) {
            for(Map.Entry<XPathFragment, XPathNode> entry : nonAttributeChildrenMap.entrySet()) {
                elements.add(new QName(entry.getKey().getNamespaceURI(), entry.getKey().getLocalName()));
            }
        }

        return elements;
    }

    /**
     * Returns the top {@code XPathNodeWrapper} from the stack.
     *
     * @return top {@code XPathNodeWrapper} from the stack or {@code null} if the stack is empty.
     */
    private XPathNodeWrapper getCurrentNodeWrapper() {
        final XPathNodeWrapper nodeWrapper = xPathNodes.isEmpty() ? null : xPathNodes.peek();

        if (nodeWrapper != null) {
            return nodeWrapper;
        } else {
            return null;
        }
    }

    @Override
    public Collection<QName> getExpectedAttributes() {
        final List<QName> attributes = new LinkedList<QName>();
        final XPathNodeWrapper currentNodeWrapper = getCurrentNodeWrapper();

        final Map<XPathFragment, XPathNode> attributeChildrenMap =
                currentNodeWrapper == null ? null :currentNodeWrapper.xPathNode.getAttributeChildrenMap();

        if (attributeChildrenMap != null) {
            for(Map.Entry<XPathFragment, XPathNode> entry : attributeChildrenMap.entrySet()) {
                attributes.add(new QName(entry.getKey().getNamespaceURI(), entry.getKey().getLocalName()));
            }
        }

        return attributes;
    }

    @Override
    public void startElement(final QName name) {
        if (name == null || firstDocumentElement) {
            firstDocumentElement = false;

            if (name != null) {
                createRootNodeWrapperForExpectedElement(name);
            }

            return;
        }

        XPathNode childNode = null;
        XPathNodeWrapper newNodeWrapper = null;

        // find our child node
        final XPathNodeWrapper currentNodeWrapper = getCurrentNodeWrapper();
        final Map<XPathFragment, XPathNode> nonAttributeChildrenMap =
                currentNodeWrapper == null ? null : currentNodeWrapper.xPathNode.getNonAttributeChildrenMap();

        if (nonAttributeChildrenMap != null) {
            for (Map.Entry<XPathFragment, XPathNode> child : nonAttributeChildrenMap.entrySet()) {
                if (name.getLocalPart().equalsIgnoreCase(child.getKey().getLocalName())) {
                    childNode = child.getValue();
                    break;
                }
            }

            if (childNode != null) {
                final MappingNodeValue nodeValue = (MappingNodeValue) childNode.getNodeValue();

                if (nodeValue != null) {
                    ClassDescriptor descriptor = nodeValue.getMapping().getReferenceDescriptor();

                    if (descriptor == null && !isReader) {
                        descriptor = nodeValue.getMapping().getDescriptor();
                    }

                    if (descriptor != null) {
                        TreeObjectBuilder objectBuilder = (TreeObjectBuilder) descriptor.getObjectBuilder();
                        final XPathNodeWrapper nodeWrapper = getCurrentNodeWrapper();

                        newNodeWrapper = new XPathNodeWrapper(
                                objectBuilder.getRootXPathNode(),
                                nodeWrapper,
                                nodeValue, descriptor, name);

                        xPathNodes.push(newNodeWrapper);
                    }
                }
            }
        }
        lastAccessedNode = newNodeWrapper == null ? new XPathNodeWrapper(name) : newNodeWrapper;
    }

    @Override
    public void endElement(final QName name) {
        final XPathNodeWrapper xPathNodeWrapper = getCurrentNodeWrapper();

        if (xPathNodeWrapper != null && xPathNodeWrapper.name.equals(name)) {
            xPathNodes.pop();
        }

        lastAccessedNode = getCurrentNodeWrapper();
    }

    @Override
    public Map<String, QName> getExpectedElementsMap() {
        return getCurrentNodeWrapper() == null ?
                Collections.<String, QName>emptyMap() : getCurrentNodeWrapper().getExpectedElementsMap();
    }

    @Override
    public Map<String, QName> getExpectedAttributesMap() {
        return getCurrentNodeWrapper() == null ?
                Collections.<String, QName>emptyMap() : getCurrentNodeWrapper().getExpectedAttributesMap();
    }

    @Override
    public Type getEntityType(QName entity, boolean isAttribute) {
        final XPathNodeWrapper currentNodeWrapper = getCurrentNodeWrapper();
        final ClassDescriptor classDescriptor = currentNodeWrapper == null ? null : currentNodeWrapper.getClassDescriptor();

        if (classDescriptor != null) {
            if (currentNodeWrapper.name.equals(entity)) {
                return classDescriptor.getJavaClass();
            } else {
                final EntityType entityType = currentNodeWrapper.getEntitiesTypesMap(isAttribute).get(entity.getLocalPart());
                return entityType == null ? null : entityType.type;
            }
        }

        return null;
    }

    @Override
    public boolean isArrayCollection() {
        final XPathNodeWrapper currentNodeWrapper = getCurrentNodeWrapper();

        if (currentNodeWrapper != null && lastAccessedNode != null
                && lastAccessedNode.name == currentNodeWrapper.name) {
            final MappingNodeValue nodeValue = currentNodeWrapper.getNodeValue();

            return nodeValue != null && nodeValue.isContainerValue();
        } else {
            return false;
        }
    }

    @Override
    public boolean isSameArrayCollection() {
        final int size = xPathNodes.size();
        if (size >= 2) {
            final XPathNodeWrapper last = xPathNodes.peek();
            final XPathNodeWrapper beforeLast = xPathNodes.get(size - 2);

            if (last.isInSameArrayAs(beforeLast)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasSubElements() {
        final Collection<QName> expectedElements = getExpectedElements();
        return expectedElements != null && !expectedElements.isEmpty();
    }

}

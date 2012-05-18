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
package com.sun.jersey.json.impl.reader;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.json.impl.JSONHelper;
import com.sun.jersey.json.impl.JaxbXmlDocumentStructure;

import org.codehaus.jackson.JsonParser;

/**
 * {@code XmlEventProvider} for JSON in natural notation.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class NaturalNotationEventProvider extends XmlEventProvider {

    private final boolean attrsWithPrefix;

    /**
     * Document structure to obtain the expected elements and attributes from.
     */
    private JaxbXmlDocumentStructure documentStructure;

    public NaturalNotationEventProvider(final JsonParser parser, final JSONConfiguration configuration,
                                        final JAXBContext jaxbContext, final Class<?> expectedType) throws XMLStreamException {
        super(parser, configuration);

        this.documentStructure = JSONHelper.getXmlDocumentStructure(jaxbContext, expectedType, true);

        attrsWithPrefix = configuration.isUsingPrefixesAtNaturalAttributes();
    }

    private QName getFieldQName(final String jsonFieldName, final boolean isAttribute) {
        final QName result = isAttribute
                ? documentStructure.getExpectedAttributesMap().get(jsonFieldName)
                : documentStructure.getExpectedElementsMap().get(jsonFieldName);

        return result == null ? new QName(jsonFieldName) : result;
    }

    @Override
    protected String getAttributeName(final String jsonFieldName) {
        return attrsWithPrefix ? super.getAttributeName(jsonFieldName) : jsonFieldName;
    }

    @Override
    protected QName getAttributeQName(final String jsonFieldName) {
        return getFieldQName(getAttributeName(jsonFieldName), true);
    }

    @Override
    protected QName getElementQName(final String jsonFieldName) {
        return getFieldQName(jsonFieldName, false);
    }

    @Override
    protected boolean isAttribute(String jsonFieldName) {
        jsonFieldName = getAttributeName(jsonFieldName);

        return !"$".equals(jsonFieldName)
                && (documentStructure.canHandleAttributes()
                    ? documentStructure.getExpectedAttributesMap().containsKey(jsonFieldName)
                    : !documentStructure.getExpectedElementsMap().containsKey(jsonFieldName));
    }

    @Override
    protected JsonXmlEvent createEndElementEvent(final QName elementName, final Location location) {
        documentStructure.endElement(elementName);

        return super.createEndElementEvent(elementName, location);
    }

    @Override
    protected JsonXmlEvent createStartElementEvent(final QName elementName, final Location location) {
        documentStructure.startElement(elementName);

        return super.createStartElementEvent(elementName, location);
    }

}

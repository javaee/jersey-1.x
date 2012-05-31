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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import com.sun.jersey.api.json.JSONConfiguration;

import org.codehaus.jackson.JsonParser;

/**
 * {@code XmlEventProvider} for JSON in mapped notation.
 *
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
class MappedNotationEventProvider extends XmlEventProvider {
    
    private final Map<String, String> jsonNs2XmlNs = new HashMap<String, String>();

    private final char nsSeparator;
    private final CharSequence nsSeparatorAsSequence;
    private final Collection<String> arrays;

    protected MappedNotationEventProvider(final JsonParser parser, final JSONConfiguration configuration)
            throws XMLStreamException {
        super(parser, configuration);

        nsSeparator = configuration.getNsSeparator();
        nsSeparatorAsSequence = new StringBuffer(1).append(nsSeparator);

        arrays = configuration.getArrays();

        // xmlNs-jsonNs -> jsonNs-xmlNs
        final Map<String, String> xml2JsonNs = configuration.getXml2JsonNs();
        if (xml2JsonNs != null) {
            for (Map.Entry<String, String> entry : xml2JsonNs.entrySet()) {
                jsonNs2XmlNs.put(entry.getValue(), entry.getKey());
            }
        }
    }

    @Override
    protected QName getAttributeQName(final String jsonFieldName) {
        return getFieldQName(getAttributeName(jsonFieldName));
    }

    @Override
    protected QName getElementQName(final String jsonFieldName) {
        return getFieldQName(jsonFieldName);
    }

    private QName getFieldQName(final String jsonFieldName) {
        if (jsonNs2XmlNs.isEmpty() || !jsonFieldName.contains(nsSeparatorAsSequence)) {
            return new QName(jsonFieldName);
        } else {
            int dotIndex = jsonFieldName.indexOf(nsSeparator);
            String prefix = jsonFieldName.substring(0, dotIndex);
            String suffix = jsonFieldName.substring(dotIndex + 1);
            return jsonNs2XmlNs.containsKey(prefix) ? new QName(jsonNs2XmlNs.get(prefix), suffix) : new QName(jsonFieldName);
        }
    }

    @Override
    protected boolean isAttribute(final String jsonFieldName) {
        return jsonFieldName.startsWith("@") || getJsonConfiguration().getAttributeAsElements().contains(jsonFieldName);
    }

}

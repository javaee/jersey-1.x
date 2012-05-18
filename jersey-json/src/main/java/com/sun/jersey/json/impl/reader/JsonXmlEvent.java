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

import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;

/**
 * Common parent for xml event types used by {@code XmlEventProvider}.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 * @see XmlEventProvider
 * @see JsonXmlStreamReader
 */
abstract class JsonXmlEvent {

    /**
     * Helper class representing an attribute of the xml event. Applicable only if the event is of type {@code
     * XMLStreamConstants.START_ELEMENT}.
     */
    public static class Attribute {

        private final QName name;
        private final String value;

        public Attribute(QName name, String value) {
            this.name = name;
            this.value = value;
        }

        public QName getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

    }

    /**
     * Type of this event.
     */
    private final int eventType;

    /**
     * Location of this event in JSON stream.
     */
    private final Location location;

    /**
     * Attributes of this event if this event is of {@code XMLStreamConstants.START_ELEMENT} type.
     */
    private List<Attribute> attributes;

    /**
     * Name of this event if this event is of {@code XMLStreamConstants.START_ELEMENT} or {@code XMLStreamConstants
     * .END_ELEMENT} type.
     */
    private QName name;

    /**
     * Text contents of this event if this event is of {@code XMLStreamConstants.CHARACTERS} type.
     */
    private String text;

    protected JsonXmlEvent(final int eventType, final Location location) {
        this.location = location;
        this.eventType = eventType;
    }

    /**
     * Returns a list of attributes of this event. The event needs to be of {@code XMLStreamConstants.START_ELEMENT} type to
     * return a non-null value. {@code null} value of this property can also mean that the list of attributes has not been
     * initialized yet.
     *
     * @return list of attributes or {@code null} value if this event is of incorrect type or the list of attributes has not
     * been initialized yet.
     */
    public List<Attribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns the type of this event.
     *
     * @return type of this event.
     */
    public int getEventType() {
        return eventType;
    }

    /**
     * Returns the location of this event in JSON stream.
     *
     * @return location of this event in JSON stream.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns the name of this event if this event is of {@code XMLStreamConstants.START_ELEMENT} or {@code XMLStreamConstants
     * .END_ELEMENT} type.
     *
     * @return name of this event or the {@code null} value.
     */
    public QName getName() {
        return name;
    }

    /**
     * Returns the prefix of the name of this event if this event is of {@code XMLStreamConstants.START_ELEMENT}
     * or {@code XMLStreamConstants.END_ELEMENT} type.
     *
     * @return prefix of this event or the {@code null} value.
     */
    public String getPrefix() {
        return name == null ? null : name.getPrefix();
    }

    /**
     * Returns the text of this event if this event is of {@code XMLStreamConstants.CHARACTERS} type.
     *
     * @return text of this event or the {@code null} value.
     */
    public String getText() {
        return text;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

}

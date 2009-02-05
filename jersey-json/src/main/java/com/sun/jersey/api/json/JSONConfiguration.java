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
package com.sun.jersey.api.json;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Immutable bag of various JSON notation related configuration options
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public class JSONConfiguration {

    /**
     * Enumeration of supported JSON notations.
     */
    public enum Notation {

        /**
         * The mapped (default) JSON notation.
         */
        MAPPED,
        /**
         * The mapped Jettison JSON notation.
         */
        MAPPED_JETTISON,
        /**
         * The mapped Badgerfish JSON notation.
         */
        BADGERFISH,
        /**
         * The natural JSON notation, leveraging tight JAXB RI integration.
         */
        NATURAL
    };
    private final Notation notation;
    private final Collection<String> arrays;
    private final Collection<String> attrsAsElems;
    private final Collection<String> nonStrings;
    private final boolean rootUnwrapping;
    private final Map<String, String> jsonXml2JsonNs;

    /**
     *  Builder class for constructing {@link JSONConfiguration} options bag
     */
    public static class Builder {

        private final Notation notation;
        private Collection<String> arrays = new HashSet<String>(0);
        private Collection<String> attrsAsElems = new HashSet<String>(0);
        private Collection<String> nonStrings = new HashSet<String>(0);
        private boolean rootUnwrapping = true;
        private Map<String, String> jsonXml2JsonNs = new HashMap<String, String>();

        private Builder(Notation notation) {
            this.notation = notation;
        }

        /**
         *  Constructs a new immutable {@link JSONConfiguration} object based on options set on this Builder
         *
         * @return a non-null {@link JSONConfiguration} instance
         */
        public JSONConfiguration build() {
            return new JSONConfiguration(this);
        }

        /**
         * Setter for JSON array configuration property.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
         * <p>
         * The property value is a collection of strings representing JSON object names.
         * Those objects will be declared as arrays in  JSON document.
         * <p>
         * For example, consider that the the property value is not set and the
         * JSON document is <code>{ ..., "arr1":"single element", ... }</code>.
         * If the property value is set to contain <code>"arr1"</code> then
         * the JSON document would become <code>{ ..., "arr1":["single element"], ... }</code>.
         * <p>
         * The default value is an empty collection.
         */
        public Builder setArrays(Collection<String> arrays) {
            this.arrays = arrays;
            return this;
        }

        /**
         * Setter for JSON attributes as elements property.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
         * <p>
         * The value is a collection of string values that are
         * object names that correspond to XML attribute information items.
         * The value of an object name in the JSON document that exists in the collection
         * of object names will be declared as an element as not as an attribute if
         * the object corresponds to an XML attribute information item.
         * <p>
         * For example, consider that the property value is not set and the
         * JSON document is <code>{ ..., "@number":"12", ... }</code>.
         * If the property value is set to contain <code>"number"</code>
         * then the JSON document would be <code>{ ..., "number":"12", ... }</code>.
         * <p>
         * The default value is an empty collection.
         */
        public Builder setAttrsAsElems(Collection<String> attrsAsElems) {
            this.attrsAsElems = attrsAsElems;
            return this;
        }

        /**
         * Setter for XML to JSON namespace mapping.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED_JETTISON} notation only.
         * <p>
         * The value is a map with zero or more
         * key/value pairs, where the key is an XML namespace and the value
         * is the prefix to use as the replacement for the XML namespace.
         * <p>
         * The default value is a map with zero key/value pairs.
         */
        public Builder setJsonXml2JsonNs(Map<String, String> jsonXml2JsonNs) {
            this.jsonXml2JsonNs = jsonXml2JsonNs;
            return this;
        }

        /**
         * Setter for JSON non-string values property.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
         * <p>
         * The value is collection of string values that are
         * object names.
         * The value of an object name in the JSON document that exists in the collection
         * of object names will be declared as non-string value, which is not surrounded
         * by double quotes.
         * <p>
         * For example, consider that the the property value is not set and the
         * JSON document is <code>{ ..., "anumber":"12", ... }</code>.
         * If the property value is set to contain <code>"anumber"</code>
         * then the JSON document would be <code>{ ..., "anumber":12, ... }</code>.
         * <p>
         * The default value is an empty collection.
         */
        public Builder setNonStrings(Collection<String> nonStrings) {
            this.nonStrings = nonStrings;
            return this;
        }

        /**
         * Setter for XML root element unwrapping.
         * This property is valid for the {@link JSONConfiguration.Notation#MAPPED} notation only.
         * <p>
         * If set to true, JSON code corresponding to the XML root element will be stripped out
         * <p>
         * The default value is false.
         */
        public Builder setRootUnwrapping(boolean rootUnwrapping) {
            this.rootUnwrapping = rootUnwrapping;
            return this;
        }
    }

    private JSONConfiguration(Builder b) {
        notation = b.notation;
        arrays = b.arrays;
        attrsAsElems = b.attrsAsElems;
        nonStrings = b.nonStrings;
        rootUnwrapping = b.rootUnwrapping;
        jsonXml2JsonNs = b.jsonXml2JsonNs;
    }
    /**
     * The default JSONConfiguration uses {@link JSONConfiguration.Notation#MAPPED} notation with root unwrapping option set to true.
     */
    public static JSONConfiguration DEFAULT = getBuilder(Notation.MAPPED).setRootUnwrapping(true).build();

    /**
     * A static method for obtaining a builder of {@link JSONConfiguration} instance.
     * After getting the builder, you can set configuration options on it and finally get an immutable  JSONConfiguration
     * bag built for you using {@link Builder#build() } method.
     *
     * @param notation is the JSON notation, which will be used
     * @return a builder for JSONConfiguration instance
     */
    public static Builder getBuilder(Notation notation) {
        return new Builder(notation);
    }

    public Collection<String> getArrays() {
        return (arrays != null) ? Collections.unmodifiableCollection(arrays) : null;
    }

    public Collection<String> getAttrsAsElems() {
        return (attrsAsElems != null) ? Collections.unmodifiableCollection(attrsAsElems) : null;
    }

    public Map<String, String> getJsonXml2JsonNs() {
        return (jsonXml2JsonNs != null) ? Collections.unmodifiableMap(jsonXml2JsonNs) : null;
    }

    public Collection<String> getNonStrings() {
        return (nonStrings != null) ? Collections.unmodifiableCollection(nonStrings) : null;
    }

    public Notation getNotation() {
        return notation;
    }

    public boolean isRootUnwrapping() {
        return rootUnwrapping;
    }
}

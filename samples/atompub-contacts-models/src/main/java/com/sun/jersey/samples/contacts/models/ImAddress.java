/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.samples.contacts.models;

import org.apache.abdera.model.Element;

/**
 * <p>Model class representing an instant messaging address.</p>
 */
public class ImAddress extends Base {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a default {@link ImAddress} instance.</p>
     */
    public ImAddress() {
    }


    /**
     * <p>Construct a configured {@link ImAddress} instance.</p>
     */
    public ImAddress(String address, String label, boolean primary, String protocol, String type) {
        setAddress(address);
        setLabel(label);
        setPrimary(primary);
        setProtocol(protocol);
        setType(type);
    }


    // ------------------------------------------------------ Instance Variables


    private String address;             // Email address
    private String label;               // Optional textual label
    private boolean primary;            // Flag to mark primary
    private String protocol;            // Protocol URI
    private String type;                // Programmatic type --> rel


    // ---------------------------------------------------------- Public Methods


    @Override
    public boolean equals(Object o) {
        if (o instanceof ImAddress) {
            ImAddress other = (ImAddress) o;
            return match(getAddress(), other.getAddress()) &&
                   match(getLabel(), other.getLabel()) &&
                   match(isPrimary(), other.isPrimary()) &&
                   match(getProtocol(), other.getProtocol()) &&
                   match(getType(), other.getType());
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        int value = 0;
        if (address != null) {
            value ^= address.hashCode();
        }
        if (label != null) {
            value ^= label.hashCode();
        }
        if (primary) {
            value ^= 1;
        }
        if (protocol != null) {
            value ^= protocol.hashCode();
        }
        if (type != null) {
            value ^= type.hashCode();
        }
        return value;
    }


    /**
     * <p>Return this {@link ImAddress} as an Atom <code>Element</code> suitable for
     * inclusion in a web service request or response.</p>
     */
    public Element asElement() {
        Element element = abdera.getFactory().newExtensionElement(IM_ADDRESS_QNAME);
        if (getAddress() != null) {
            element.setAttributeValue("address", getAddress());
        }
        if (getLabel() != null) {
            element.setAttributeValue("label", getLabel());
        }
        if (isPrimary()) {
            element.setAttributeValue("primary", "true");
        }
        if (getProtocol() != null) {
            element.setAttributeValue("protocol", getProtocol());
        }
        if (getType() != null) {
            element.setAttributeValue("rel", getType());
        }
        return element;
    }


    /**
     * <p>Return a new {@link ImAddress} created from the contents of the
     * specified <code>Element</code>.</p>
     *
     * @param element Atom <code>Element</code> containing our details
     */
     public static ImAddress fromElement(Element element) {
         ImAddress instance = new ImAddress();
         instance.setAddress(element.getAttributeValue("address"));
         instance.setLabel(element.getAttributeValue("label"));
         if ("true".equals(element.getAttributeValue("primary"))) {
             instance.setPrimary(true);
         }
         instance.setProtocol(element.getAttributeValue("protocol"));
         instance.setType(element.getAttributeValue("rel"));
         return instance;
     }


    // --------------------------------------------------------- Private Methods


    // -------------------------------------------------------- Property Methods


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("imAddress:{type:%s,label:%s,address:%s}", type, label, address);
    }
}

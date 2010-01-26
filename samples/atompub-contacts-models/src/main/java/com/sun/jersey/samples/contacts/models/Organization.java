/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * <p>Model class representing an organization.</p>
 */
public class Organization extends Base {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a default {@link Organization} instance.</p>
     */
    public Organization() {
    }


    /**
     * <p>Construct a configured {@link Organization} instance.</p>
     */
    public Organization(String orgName, String orgTitle, String label, boolean primary, String type) {
        setOrgName(orgName);
        setOrgTitle(orgTitle);
        setLabel(label);
        setPrimary(primary);
        setType(type);
    }


    // ------------------------------------------------------ Instance Variables


    private String label;               // Optional textual label
    private boolean primary;            // Flag to mark primary
    private String orgName;             // Organization name
    private String orgTitle;            // Title of person within organization
    private String type;                // Programmatic type --> rel


    // ---------------------------------------------------------- Public Methods


    @Override
    public boolean equals(Object o) {
        if (o instanceof Organization) {
            Organization other = (Organization) o;
            return match(getLabel(), other.getLabel()) &&
                   match(isPrimary(), other.isPrimary()) &&
                   match(getOrgName(), other.getOrgName()) &&
                   match(getOrgTitle(), other.getOrgTitle()) &&
                   match(getType(), other.getType());
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        int value = 0;
        if (label != null) {
            value ^= label.hashCode();
        }
        if (primary) {
            value ^= 1;
        }
        if (orgName != null) {
            value ^= orgName.hashCode();
        }
        if (orgTitle != null) {
            value ^= orgTitle.hashCode();
        }
        if (type != null) {
            value ^= type.hashCode();
        }
        return value;
    }


    /**
     * <p>Return this {@link Organization} as an Atom <code>Element</code> suitable for
     * inclusion in a web service request or response.</p>
     */
    public Element asElement() {
        Element element = abdera.getFactory().newExtensionElement(ORGANIZATION_QNAME);
        if (getLabel() != null) {
            element.setAttributeValue("label", getLabel());
        }
        if (getOrgName() != null) {
            Element subelement = abdera.getFactory().newExtensionElement(ORGANIZATION_NAME_QNAME, element);
            subelement.setText(getOrgName());
        }
        if (getOrgTitle() != null) {
            Element subelement = abdera.getFactory().newExtensionElement(ORGANIZATION_TITLE_QNAME, element);
            subelement.setText(getOrgTitle());
        }
        if (isPrimary()) {
            element.setAttributeValue("primary", "true");
        }
        if (getType() != null) {
            element.setAttributeValue("rel", getType());
        }
        return element;
    }


    /**
     * <p>Return a new {@link Organization} created from the contents of the
     * specified <code>Element</code>.</p>
     *
     * @param element Atom <code>Element</code> containing our details
     */
     public static Organization fromElement(Element element) {
         Organization instance = new Organization();
         if (element.getElements() != null) {
             for (Element subelement : element.getElements()) {
                 if (ORGANIZATION_NAME_QNAME.equals(subelement.getQName())) {
                     instance.setOrgName(subelement.getText());
                 } else if (ORGANIZATION_TITLE_QNAME.equals(subelement.getQName())) {
                     instance.setOrgTitle(subelement.getText());
                 }
             }

         }
         instance.setLabel(element.getAttributeValue("label"));
         if ("true".equals(element.getAttributeValue("primary"))) {
             instance.setPrimary(true);
         }
         instance.setType(element.getAttributeValue("rel"));
         return instance;
     }


    // --------------------------------------------------------- Private Methods


    // -------------------------------------------------------- Property Methods


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgTitle() {
        return orgTitle;
    }

    public void setOrgTitle(String orgTitle) {
        this.orgTitle = orgTitle;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("organization:{type:%s,label:%s,title:%s,name:%s}", type, label, orgTitle, orgName);
    }
}

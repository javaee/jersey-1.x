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

import java.util.Date;
import javax.xml.namespace.QName;
import org.apache.abdera.Abdera;

/**
 * <p>Base class to share resources across model classes.</p>
 */
public abstract class Base {


    /**
     * <p>The {@link Abdera} instance that serves as our factory object.</p>
     */
    protected static final Abdera abdera = Abdera.getInstance();


    /**
     * <p>The extension namespace prefix we prefer.</p>
     */
    public static final String NAMESPACE_PREFIX = "cd";


    /**
     * <p>The extension namespace URI to use in elements we create.</p>
     */
    public static final String NAMESPACE_URI =
            "http://example.com/contacts";


    /**
     * <p>The categories scheme URI to use in elements we create.</p>
     */
    public static final String CATEGORIES_SCHEME_URI =
            "http://example.com/categories";


    /**
     * <p>Fully qualified name of an email address element.</p>
     */
    public static final QName EMAIL_ADDRESS_QNAME =
            new QName(NAMESPACE_URI, "email");


    /**
     * <p>Fully qualified name of an email address element.</p>
     */
    public static final QName IM_ADDRESS_QNAME =
            new QName(NAMESPACE_URI, "im");


    /**
     * <p>Fully qualified name of an organization element.</p>
     */
    public static final QName ORGANIZATION_QNAME =
            new QName(NAMESPACE_URI, "organization");


    /**
     * <p>Fully qualified name of an organization name element.</p>
     */
    public static final QName ORGANIZATION_NAME_QNAME =
            new QName(NAMESPACE_URI, "orgName");


    /**
     * <p>Fully qualified name of an organization title element.</p>
     */
    public static final QName ORGANIZATION_TITLE_QNAME =
            new QName(NAMESPACE_URI, "orgTitle");


    /**
     * <p>Fully qualified name of a password element.</p>
     */
    public static final QName PASSWORD_QNAME =
            new QName(NAMESPACE_URI, "password");


    /**
     * <p>Fully qualified name of a postal address element.</p>
     */
    public static final QName POSTAL_ADDRESS_QNAME =
            new QName(NAMESPACE_URI, "postalAddress");


    /**
     * <p>Fully qualified name of a phone number element.</p>
     */
    public static final QName PHONE_NUMBER_QNAME =
            new QName(NAMESPACE_URI, "phoneNumber");


    /**
     * <p>Relationship type for "home".</p>
     */
    public static final String REL_TYPE_HOME =
            NAMESPACE_URI + "#home";


    /**
     * <p>Relationship type for "other".</p>
     */
    public static final String REL_TYPE_OTHER =
            NAMESPACE_URI + "#other";


    /**
     * <p>Relationship type for "work".</p>
     */
    public static final String REL_TYPE_WORK =
            NAMESPACE_URI + "#work";


    /**
     * <p>Fully qualified name of a username element.</p>
     */
    public static final QName USERNAME_QNAME =
            new QName(NAMESPACE_URI, "username");


    // ------------------------------------------------------- Protected Methods


    protected boolean match(boolean first, boolean second) {
        return (first == second);
    }


    protected boolean match(Date first, Date second) {
        if (first != null) {
            return first.equals(second);
        } else {
            return (second == null);
        }
    }


    protected boolean match(String first, String second) {
        if (first != null) {
            return first.equals(second);
        } else {
            return (second == null);
        }
    }


}

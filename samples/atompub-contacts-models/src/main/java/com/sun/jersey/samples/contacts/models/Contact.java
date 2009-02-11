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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Entry;

/**
 * <p>Model class representing a contact.</p>
 */
public class Contact extends Base {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a default {@link Contact} instance.</p>
     */
    public Contact() {
        setContent("");
        setName("");
    }


    // ------------------------------------------------------ Instance Variables


    private List<String> categories;              // Category tags
    private String content;                       // Comments about this contact
    private List<EmailAddress> emailAddresses;    // Email addresses
    private String id = "";                       // Unique identifier
    private List<ImAddress> imAddresses;          // IM addresses
    private String name;                          // Contact name (-> title)
    private List<Organization> organizations;     // Organizations
    private List<PhoneNumber> phoneNumbers;       // Phone numbers
    private List<PostalAddress> postalAddresses;  // Postal addresses
    private Date updated = new Date();            // Last updated date/time


    // ---------------------------------------------------------- Public Methods


    @Override
    public boolean equals(Object o) {
        if (o instanceof Contact) {
            Contact other = (Contact) o;
            return getCategories().equals(other.getCategories()) &&
                   match(getContent(), other.getContent()) &&
                   getEmailAddresses().equals(other.getEmailAddresses()) &&
                   match(getId(), other.getId()) &&
                   getImAddresses().equals(other.getImAddresses()) &&
                   match(getName(), other.getName()) &&
                   getOrganizations().equals(other.getOrganizations()) &&
                   getPhoneNumbers().equals(other.getPhoneNumbers()) &&
                   getPostalAddresses().equals(other.getPostalAddresses()) &&
                   match(getUpdated(), other.getUpdated());
        } else {
            return false;
        }
    }


    @Override
    public int hashCode() {
        int value = 0;
        value ^= getCategories().hashCode();
        if (content != null) {
            value ^= content.hashCode();
        }
        value ^= getEmailAddresses().hashCode();
        value ^= getImAddresses().hashCode();
        if (name != null) {
            value ^= name.hashCode();
        }
        value ^= getOrganizations().hashCode();
        if (id != null) {
            value ^= getId().hashCode();
        }
        value ^= getPhoneNumbers().hashCode();
        value ^= getPostalAddresses().hashCode();
        value ^= getUpdated().hashCode();
        return value;
    }


    /**
     * <p>Return this {@link Contact} as an Atom {@link Entry} suitable for
     * inclusion in a web service request or response.  Note that no Atom
     * links will have been defined, because they depend upon the server
     * environment in which these instances are embedded.</p>
     */
    public Entry asEntry() {
        Entry entry = abdera.newEntry();
        for (String term : getCategories()) {
            entry.addCategory(CATEGORIES_SCHEME_URI, term, null);
        }
        entry.setContent(getContent());
        for (EmailAddress emailAddress : getEmailAddresses()) {
            entry.addExtension(emailAddress.asElement());
        }
        entry.setId(getId());
        for (ImAddress imAddress : getImAddresses()) {
            entry.addExtension(imAddress.asElement());
        }
        for (Organization organization : getOrganizations()) {
            entry.addExtension(organization.asElement());
        }
        for (PhoneNumber phoneNumber : getPhoneNumbers()) {
            entry.addExtension(phoneNumber.asElement());
        }
        for (PostalAddress postalAddress : getPostalAddresses()) {
            entry.addExtension(postalAddress.asElement());
        }
        entry.setTitle(getName());
        entry.setUpdated(getUpdated());
        return entry;
    }


    /**
     * <p>Return a new {@link Contact} created from the contents of the
     * specified {@link Entry}.</p>
     *
     * @param entry Atom {@link Entry} containing our details
     */
     public static Contact fromEntry(Entry entry) {
         Contact instance = new Contact();
         for (Category category : entry.getCategories(CATEGORIES_SCHEME_URI)) {
             instance.getCategories().add(category.getTerm());
         }
         instance.setContent(entry.getContent());
         for (Element element : entry.getExtensions(EMAIL_ADDRESS_QNAME)) {
             instance.getEmailAddresses().add(EmailAddress.fromElement(element));
         }
         for (Element element : entry.getExtensions(IM_ADDRESS_QNAME)) {
             instance.getImAddresses().add(ImAddress.fromElement(element));
         }
         instance.setId(entry.getId().toString());
         instance.setName(entry.getTitle());
         for (Element element : entry.getExtensions(ORGANIZATION_QNAME)) {
             instance.getOrganizations().add(Organization.fromElement(element));
         }
         for (Element element : entry.getExtensions(PHONE_NUMBER_QNAME)) {
             instance.getPhoneNumbers().add(PhoneNumber.fromElement(element));
         }
         for (Element element : entry.getExtensions(POSTAL_ADDRESS_QNAME)) {
             instance.getPostalAddresses().add(PostalAddress.fromElement(element));
         }
         instance.setUpdated(entry.getUpdated());
         return instance;
     }


     /**
      * <p>Update the fields of this {@link Contact} from the specified
      * update information.</p>
      *
      * @param update {@link Contact} containing updated information
      */
     public void updateFrom(Contact update) {
         this.categories = update.getCategories();
         this.content = update.getContent();
         this.emailAddresses = update.getEmailAddresses();
         // "id" skipped deliberately
         this.imAddresses = update.getImAddresses();
         this.name = update.getName();
         this.organizations = update.getOrganizations();
         this.phoneNumbers = update.getPhoneNumbers();
         this.postalAddresses = update.getPostalAddresses();
         this.updated = new Date();
     }


    // --------------------------------------------------------- Private Methods


    // -------------------------------------------------------- Property Methods


    public List<String> getCategories() {
        if (categories == null) {
            categories = new ArrayList<String>();
        }
        return categories;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<EmailAddress> getEmailAddresses() {
        if (emailAddresses == null) {
            emailAddresses = new ArrayList<EmailAddress>();
        }
        return emailAddresses;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ImAddress> getImAddresses() {
        if (imAddresses == null) {
            imAddresses = new ArrayList<ImAddress>();
        }
        return imAddresses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Organization> getOrganizations() {
        if (organizations == null) {
            organizations = new ArrayList<Organization>();
        }
        return organizations;
    }

    public List<PhoneNumber> getPhoneNumbers() {
        if (phoneNumbers == null) {
            phoneNumbers = new ArrayList<PhoneNumber>();
        }
        return phoneNumbers;
    }

    public List<PostalAddress> getPostalAddresses() {
        if (postalAddresses == null) {
            postalAddresses = new ArrayList<PostalAddress>();
        }
        return postalAddresses;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return String.format("contact:{id:%s,name:%s,content:%s}", id, name, content);
    }
}

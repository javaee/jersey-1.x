/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.json.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author japod
 */
@XmlRootElement
public class NullStringBean {

    @XmlElement(nillable=true)
    public String nullString = "not null to test if set to null works";

    public static Object createTestInstance() {
        return new NullStringBean();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NullStringBean other = (NullStringBean) obj;
        if ((this.nullString == null) ? (other.nullString != null) : !this.nullString.equals(other.nullString)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.nullString != null ? this.nullString.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{nullString:%s}", quoteDelimitedStringOrNull(nullString));
    }

    private String quoteDelimitedStringOrNull(String string) {
        return (string == null) ? "null" : String.format("\"%s\"", string);
    }
}

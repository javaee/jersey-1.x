/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.json;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author japod
 */
@XmlRootElement
public class ComplexBeanWithAttributes {

    @XmlAttribute public String a1;
    @XmlAttribute public int a2;
    @XmlElement public String filler1;
    @XmlElement public List<SimpleBeanWithAttributes> list;
    @XmlElement public String filler2;
    @XmlElement SimpleBeanWithAttributes b;

    public static Object createTestInstance() {
        ComplexBeanWithAttributes instance = new ComplexBeanWithAttributes();
        instance.a1 = "hello dolly";
        instance.a2 = 31415926;
        instance.filler1 = "111";
        instance.filler2 = "222";
        instance.b = (SimpleBeanWithAttributes)SimpleBeanWithAttributes.createTestInstance();
        instance.list = new LinkedList<SimpleBeanWithAttributes>();
        instance.list.add((SimpleBeanWithAttributes)SimpleBeanWithAttributes.createTestInstance());
        instance.list.add((SimpleBeanWithAttributes)SimpleBeanWithAttributes.createTestInstance());
        return instance;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ComplexBeanWithAttributes)) {
            return false;
        }
        final ComplexBeanWithAttributes other = (ComplexBeanWithAttributes) obj;
        if (this.a1 != other.a1 && (this.a1 == null || !this.a1.equals(other.a1))) {
            return false;
        }
        if (this.a2 != other.a2) {
            return false;
        }
        if (this.b != other.b && (this.b == null || !this.b.equals(other.b))) {
            return false;
        }
        if (this.filler1 != other.filler1 && (this.filler1 == null || !this.filler1.equals(other.filler1))) {
            return false;
        }
        if (this.filler2 != other.filler2 && (this.filler2 == null || !this.filler2.equals(other.filler2))) {
            return false;
        }
        if (this.list != other.list && (this.list == null || !this.list.equals(other.list))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + (this.a1 != null ? this.a1.hashCode() : 0);
        hash = 19 * hash + this.a2;
        hash = 19 * hash + (this.b != null ? this.b.hashCode() : 0);
        hash = 19 * hash + (this.filler1 != null ? this.filler1.hashCode() : 0);
        hash = 19 * hash + (this.filler2 != null ? this.filler2.hashCode() : 0);
        hash = 19 * hash + (this.list != null ? this.list.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return (new Formatter()).format("CBWA(%s,%d,%s)", a1, a2, b).toString();
    }
}

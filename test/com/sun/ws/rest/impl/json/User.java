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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author japod
 */
@XmlRootElement
public class User {
    @XmlElement(name="userid")
    public String id;
    public String name;
    @XmlTransient
    public String password;
    
    public User() {
        this("1621", "Grotefend", "Persepolis");
    }
    
    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof User)) {
            return false;
        }
        final User other = (User) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
//        if (this.password != other.password && (this.password == null || !this.password.equals(other.password))) {
//            return false;
//        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        if (null != id) {
            hash = 17 * hash + id.hashCode();
        }
        if (null != name) {
            hash = 17 * hash + name.hashCode();
        }
//        if (null != password) {
//            hash = 17 * hash + password.hashCode();
//        }
        return hash;
    }
    
    @Override
    public String toString() {
        return "User(" + id + ", " + name + ")";
    }
    
}

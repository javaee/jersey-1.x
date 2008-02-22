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

import com.sun.ws.rest.impl.json.User;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author japod
 */
@XmlRootElement
public class UserTable {

    public static class JMakiTableHeader {

        public String id;
        public String label;

        public JMakiTableHeader() {
        }

        public JMakiTableHeader(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }
    public List<JMakiTableHeader> columns = initHeaders();
    public List<User> rows;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserTable)) {
            return false;
        }
        final UserTable other = (UserTable) obj;
        if ((this.rows == other.rows) || ((null == this.rows) && (null == other.rows))) {
            return true;
        }
        if (this.rows.size() != other.rows.size()) {
            return false;
        }
        return this.rows.containsAll(other.rows) && other.rows.containsAll(this.rows);
    }

    @Override
    public int hashCode() {
        int hash = 16;
        if (null != rows) {
            for (User u : rows) {
                hash = 17 * hash + u.hashCode(); 
            }
        }
        return hash;
    }

    static List<JMakiTableHeader> initHeaders() {
        List<JMakiTableHeader> headers = new LinkedList<JMakiTableHeader>();
        headers.add(new JMakiTableHeader("userid", "UserID"));
        headers.add(new JMakiTableHeader("name", "User Name"));
        return headers;
    }
    
    public UserTable() {}
    
    public UserTable(List<User> users) {
        this.rows = new LinkedList<User>();
        this.rows.addAll(users);
    }

    @Override
    public String toString() {
        return "UserTable(" + rows.toString() + ")";
    }
}

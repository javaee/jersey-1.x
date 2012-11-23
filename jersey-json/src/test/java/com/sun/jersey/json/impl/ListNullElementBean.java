/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.json.impl;

import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
@XmlRootElement(name = "listNullElementBean")
public class ListNullElementBean {

    private List<ListValue> list;

    public static Object createTestInstance() {
        ListNullElementBean instance = new ListNullElementBean();
        instance.list = new LinkedList<ListValue>();
        instance.list.add(new ListValue());
        return instance;
    }

    public static String[] getArrayElements() {
        return new String[] {"list"};
    }

    public List<ListValue> getList() {
        return list;
    }

    public void setList(List<ListValue> list) {
        this.list = list;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ListNullElementBean other = (ListNullElementBean) obj;
        return this.list == other.list
                || (JSONTestHelper.isCollectionEmpty(this.list) && JSONTestHelper.isCollectionEmpty(other.list))
                || (this.list != null && this.list.equals(other.list));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.list != null ? this.list.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return (new Formatter()).format("LwEEB(n=%d,isNull:%s)", (list != null) ? list.size() : 0, (list ==null)).toString();
    }

    @XmlRootElement(name = "listValue")
    public static class ListValue {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final ListValue listValue = (ListValue) o;

            if (value != null ? !value.equals(listValue.value) : listValue.value != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}

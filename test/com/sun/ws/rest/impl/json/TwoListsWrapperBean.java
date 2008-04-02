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

import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author japod
 */
@XmlRootElement(name = "item")
public class TwoListsWrapperBean {
    public List<String> property1, property2;
    
    public static Object createTestInstance() {
        TwoListsWrapperBean instance = new TwoListsWrapperBean();
        instance.property1 = new LinkedList<String>();
        instance.property1.add("a1");
        instance.property1.add("a1");
        instance.property2 = new LinkedList<String>();
        instance.property2.add("b1");
        return instance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TwoListsWrapperBean other = (TwoListsWrapperBean) obj;
        if (this.property1 != other.property1 && (this.property1 == null || !this.property1.equals(other.property1))) {
            return false;
        }
        if (this.property2 != other.property2 && (this.property2 == null || !this.property2.equals(other.property2))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.property1 != null ? this.property1.hashCode() : 0);
        hash = 59 * hash + (this.property2 != null ? this.property2.hashCode() : 0);
        return hash;
    }
}

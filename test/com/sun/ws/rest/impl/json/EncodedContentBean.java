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

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author japod
 */
@XmlRootElement
public class EncodedContentBean {

    public String one;
    public String two;
    
    public static Object createTestInstance() {
        EncodedContentBean instance = new EncodedContentBean();
        instance.one = "\tone\n\tbig";
        instance.two = "haf\u010C";
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
        final EncodedContentBean other = (EncodedContentBean) obj;
        if (this.one != other.one && (this.one == null || !this.one.equals(other.one))) {
            return false;
        }
        if (this.two != other.two && (this.two == null || !this.two.equals(other.two))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.one != null ? this.one.hashCode() : 0);
        hash = 17 * hash + (this.two != null ? this.two.hashCode() : 0);
        return hash;
    }
    
}

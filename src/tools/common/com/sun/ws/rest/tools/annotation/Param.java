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

package com.sun.ws.rest.tools.annotation;

import javax.xml.namespace.QName;

/**
 * Models a parameter to a resource method
 */
public class Param {
    
    Style style;
    String name;
    String defaultValue;
    boolean repeating;
    QName type;
    
    /** Creates a new instance of Param */
    public Param(String name, Style style, String defaultValue, QName type, boolean repeating) {
        this.name = name;
        this.style = style;
        this.defaultValue = defaultValue;
        this.type = type;
        this.repeating = repeating;
    }
    
    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public Style getStyle() {
        return style;
    }

    public QName getType() {
        return type;
    }

    public boolean isRepeating() {
        return repeating;
    }
    
    public enum Style {QUERY, URI, ENTITY, MATRIX, HEADER};
}

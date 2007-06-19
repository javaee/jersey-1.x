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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an abstract resource
 * 
 */
public class Resource {
    
    private String template;
    private String className;
    private String[] produces;
    private String[] consumes;
    private List<Method> methods;
    
    /** Creates a new instance of Resource */
    public Resource(String className, String template) {
        this.template = template;
        this.className = className;
        produces = consumes = null;
        methods = new ArrayList<Method>();
   }

    public String getClassName() {
        return className;
    }
    
    public String getTemplate() {
        return template;
    }
    
    public String[] getConsumes() {
        return consumes;
    }

    public void setConsumes(String... consumes) {
        this.consumes = consumes;
    }

    public String[] getProduces() {
        return produces;
    }

    public void setProduces(String... produces) {
        this.produces = produces;
    }

    public List<Method> getMethods() {
        return methods;
    }
}

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
 * Represents an abstract resource method
 * 
 */
public class Method {
    
    private String methodName;
    private String[] produces;
    private String[] consumes;
    private Resource parent;
    private List<Param> params;
    private boolean inputEntity;
    private boolean outputEntity;
    
    /** Creates a new instance of Method */
    public Method(String methodName, Resource parent) {
        this.methodName = methodName;
        this.parent = parent;
        parent.getMethods().add(this);
        produces = consumes = null;
        params = new ArrayList<Param>();
        inputEntity = outputEntity = false;
    }
    
    public String getConsumes() {
        return combineMimeTypeArray((consumes != null) ? consumes : parent.getConsumes());
    }

    public void setConsumes(String... consumes) {
        this.consumes = consumes;
    }

    public String getProduces() {
        return combineMimeTypeArray((produces != null) ? consumes : parent.getProduces());
    }

    public void setProduces(String... produces) {
        this.produces = produces;
    }

    public String getMethodName() {
        return methodName;
    }
    
    public List<Param> getParams() {
        return params;
    }

    public boolean isInputEntity() {
        return inputEntity;
    }

    public void setInputEntity(boolean inputEntity) {
        this.inputEntity = inputEntity;
    }

    public boolean isOutputEntity() {
        return outputEntity;
    }

    public void setOutputEntity(boolean outputEntity) {
        this.outputEntity = outputEntity;
    }
    
    private String combineMimeTypeArray(String[] mimeTypes) {
        if (mimeTypes == null)
            return null;
        
        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (String m : mimeTypes) {
            if (!first)
                b.append(',');
            
            first = false;
            b.append(m);
        }

        return b.toString();
    }
}

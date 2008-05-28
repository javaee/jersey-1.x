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
package com.sun.jersey.api.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AbstractSetterMethod implements Parameterized, AbstractModelComponent {
    private List<Parameter> parameters;
    
    private Method method;

    public AbstractSetterMethod(Method method) {
        assert null != method;

        this.method = method;
        this.parameters = new ArrayList<Parameter>();        
    }
    
    public Method getMethod() {
        return method;
    }
    
    public List<Parameter> getParameters() {
        return parameters;
    }

    public void accept(AbstractModelVisitor visitor) {
        visitor.visitAbstractSetterMethod(this);
    }

    public List<AbstractModelComponent> getComponents() {
        return null;
    }
        
    @Override
    public String toString() {
        return "AbstractSetterMethod(" 
                + getMethod().getDeclaringClass().getSimpleName() + "#" + getMethod().getName() + ")";
    }
}
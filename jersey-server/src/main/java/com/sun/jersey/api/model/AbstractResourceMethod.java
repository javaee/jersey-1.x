/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.jersey.api.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 * Abstraction for a resource method
 */
public class AbstractResourceMethod extends AbstractMethod 
        implements Parameterized, AbstractModelComponent {
    private String httpMethod;
    private List<MediaType> consumeMimeList;
    private List<MediaType> produceMimeList;
    private List<Parameter> parameters;
    private Class returnType;
    private Type genericReturnType;
    private boolean isProducesDeclared;

    public AbstractResourceMethod(
            AbstractResource resource,
            Method method,
            Class returnType,
            Type genericReturnType,
            String httpMethod,
            Annotation[] annotations) {
        super(resource, method, annotations);

        this.httpMethod = httpMethod.toUpperCase();
        this.consumeMimeList = new ArrayList<MediaType>();
        this.produceMimeList = new ArrayList<MediaType>();
        this.returnType = returnType;
        this.genericReturnType = genericReturnType;
        this.parameters = new ArrayList<Parameter>();


    }

    public AbstractResource getDeclaringResource() {
        return getResource();
    }

    public Class getReturnType() {
        return returnType;
    }

    public Type getGenericReturnType() {
        return genericReturnType;
    }

    public List<MediaType> getSupportedInputTypes() {
        return consumeMimeList;
    }

    public List<MediaType> getSupportedOutputTypes() {
        return produceMimeList;
    }

    public void setAreInputTypesDeclared(boolean declared) {
        isProducesDeclared = declared;
    }

    public boolean areInputTypesDeclared() {
        return isProducesDeclared;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }

    public boolean hasEntity() {
        for (Parameter p : getParameters()) {
            if (Parameter.Source.ENTITY == p.getSource()) {
                return true;
            }
        }
        return false;
    }
    
    public List<Parameter> getParameters() {
        return parameters;
    }

    public void accept(AbstractModelVisitor visitor) {
        visitor.visitAbstractResourceMethod(this);
    }
    
    public List<AbstractModelComponent> getComponents() {
        return null;
    }

    @Override
    public String toString() {
        return "AbstractResourceMethod(" 
                + getMethod().getDeclaringClass().getSimpleName() + "#" + getMethod().getName() + ")";
    }

}

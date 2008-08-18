/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstraction for resource class
 */
public class AbstractResource implements PathAnnotated, AbstractModelComponent {

    private final Class<?> resourceClass;
    private final PathValue uriPath;
    private final List<AbstractResourceConstructor> constructors;
    private final List<AbstractField> fields;
    private final List<AbstractSetterMethod> setterMethods;
    private final List<AbstractResourceMethod> resourceMethods;
    private final List<AbstractSubResourceMethod> subResourceMethods;
    private final List<AbstractSubResourceLocator> subResourceLocators;

    /**
     * Creates a new instance of AbstractResource
     */
    public AbstractResource(Class<?> resourceClass) {
        this(resourceClass, null);
    }

    /**
     * Creates a new instance of AbstractResource
     */
    public AbstractResource(Class<?> resourceClass, PathValue uriPath) {
        this.resourceClass = resourceClass;
        this.uriPath = uriPath;
        this.constructors = new ArrayList<AbstractResourceConstructor>();
        this.fields = new ArrayList<AbstractField>();
        this.setterMethods = new ArrayList<AbstractSetterMethod>();
        this.resourceMethods = new ArrayList<AbstractResourceMethod>();
        this.subResourceLocators = new ArrayList<AbstractSubResourceLocator>();
        this.subResourceMethods = new ArrayList<AbstractSubResourceMethod>();
    }

    public Class<?> getResourceClass() {
        return resourceClass;
    }

    public boolean isSubResource() {
        return uriPath == null;
    }

    public boolean isRootResource() {
        return uriPath != null;
    }

    public PathValue getPath() {
        return uriPath;
    }

    public List<AbstractResourceConstructor> getConstructors() {
        return constructors;
    }
    
    public List<AbstractField> getFields() {
        return fields;
    }
    
    public List<AbstractSetterMethod> getSetterMethods() {
        return setterMethods;
    }
    
    /**
     * Provides a non-null list of resource methods available on the resource
     * 
     * @return non-null abstract resource method list
     */
    public List<AbstractResourceMethod> getResourceMethods() {
        return resourceMethods;
    }

    /**
     * Provides a non-null list of subresource methods available on the resource
     * 
     * @return non-null abstract subresource method list
     */
    public List<AbstractSubResourceMethod> getSubResourceMethods() {
        return subResourceMethods;
    }

    /**
     * Provides a non-null list of subresource locators available on the resource
     * 
     * @return non-null abstract subresource locator list
     */
    public List<AbstractSubResourceLocator> getSubResourceLocators() {
        return subResourceLocators;
    }

    public void accept(AbstractModelVisitor visitor) {
        visitor.visitAbstractResource(this);
    }

    @Override
    public String toString() {
        return "AbstractResource(" 
                + ((null == getPath()) ? "" : ("\"" + getPath().getValue() + "\", - "))
                + getResourceClass().getSimpleName() + ": " 
                + getConstructors().size() + " constructors, " 
                + getFields().size() + " fields, " 
                + getSetterMethods().size() + " setter methods, " 
                + getResourceMethods().size() + " res methods, " 
                + getSubResourceMethods().size() + " subres methods, " 
                + getSubResourceLocators().size() + " subres locators " + ")";
    }

    public List<AbstractModelComponent> getComponents() {
        List<AbstractModelComponent> components = new LinkedList<AbstractModelComponent>();
        components.addAll(getConstructors());
        components.addAll(getSetterMethods());
        components.addAll(getResourceMethods());
        components.addAll(getSubResourceMethods());
        components.addAll(getSubResourceLocators());
        return components;
    }
}

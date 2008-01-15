/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * AbstractResource.java
 *
 * Created on October 5, 2007, 11:34 AM
 *
 */
package com.sun.ws.rest.api.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstraction for resource class
 */
public class AbstractResource implements UriPathAnnotated, AbstractModelComponent {

    private Class<?> resourceClass;
    private UriPathValue uriPath;
    private List<AbstractResourceMethod> resourceMethods;
    private List<AbstractSubResourceMethod> subResourceMethods;
    private List<AbstractSubResourceLocator> subResourceLocators;
    private List<AbstractResourceConstructor> constructors;

    /**
     * Creates a new instance of AbstractResource
     */
    public AbstractResource(Class<?> resourceClass) {
        this(resourceClass, null);
    }

    /**
     * Creates a new instance of AbstractResource
     */
    public AbstractResource(Class<?> resourceClass, UriPathValue uriPath) {
        this.resourceClass = resourceClass;
        this.uriPath = uriPath;
        this.constructors = new ArrayList<AbstractResourceConstructor>();
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

    public UriPathValue getUriPath() {
        return uriPath;
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

    public List<AbstractResourceConstructor> getConstructors() {
        return constructors;
    }
    
    public void accept(AbstractModelVisitor visitor) {
        visitor.visitAbstractResource(this);
    }

    @Override
    public String toString() {
        return "AbstractResource(" 
                + ((null == getUriPath()) ? "" : ("\"" + getUriPath().getValue() + "\", - ")) 
                + getResourceClass().getSimpleName() + ": " 
                + getResourceMethods().size() + " res methods, " 
                + getSubResourceMethods().size() + " subres methods, " 
                + getSubResourceLocators().size() + " subres locators " + ")";
    }

    public List<AbstractModelComponent> getComponents() {
        List<AbstractModelComponent> components = new LinkedList<AbstractModelComponent>();
        components.addAll(getConstructors());
        components.addAll(getResourceMethods());
        components.addAll(getSubResourceMethods());
        components.addAll(getSubResourceLocators());
        return components;
    }
}

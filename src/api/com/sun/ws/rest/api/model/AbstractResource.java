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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 * Abstraction for resource class
 */
public class AbstractResource implements UriTemplated {
    
    private Class<?> resourceClass;
    private UriTemplateValue uriTemplate;
    private List<AbstractResourceMethod> resourceMethods;
    private List<AbstractSubResourceMethod> subResourceMethods;
    private List<AbstractSubResourceLocator> subResourceLocators;
    private AbstractResourceConstructor ctor;
    
    private static AbstractResourceConstructor getDefaultConstructor(Class<?> resourceClass) {
        AbstractResourceConstructor result = null;
        try {
            // TODO: looks like a random selection :-(
            Constructor[] constructors = resourceClass.getConstructors();
            if (constructors.length > 0) {
                // TODO: what about parameters?
                result = new AbstractResourceConstructor(constructors[0]);
            }
        } catch (SecurityException ex) {
            // TODO: (log it?) and handle it somehow better
            ex.printStackTrace();
        }
        return result;
    }
    
    /**
     * Creates a new instance of AbstractResource
     */
    public AbstractResource(Class<?> resourceClass) {
        this(resourceClass, null, getDefaultConstructor(resourceClass));
    }
    
    /**
     * Creates a new instance of AbstractResource
     */
    public AbstractResource(Class<?> resourceClass, UriTemplateValue uriTemplate) {
        this(resourceClass, uriTemplate, getDefaultConstructor(resourceClass));
    }
    
    /**
     * Creates a new instance of AbstractResource
     */
    public AbstractResource(Class<?> resourceClass, UriTemplateValue uriTemplate, AbstractResourceConstructor constructor) {
        this.resourceClass = resourceClass;
        this.ctor = constructor;
        this.uriTemplate = uriTemplate;
        this.resourceMethods = new ArrayList<AbstractResourceMethod>();
        this.subResourceLocators = new ArrayList<AbstractSubResourceLocator>();
        this.subResourceMethods = new ArrayList<AbstractSubResourceMethod>();
    }
    
    public Class<?> getResourceClass() {
        return resourceClass;
    }
    
    public boolean isSubResource() {
        return uriTemplate==null;
    }
    
    public boolean isRootResource() {
        return uriTemplate!=null;
    }
    
    public UriTemplateValue getUriTemplate() {
        return uriTemplate;
    }
    
    public List<AbstractResourceMethod> getResourceMethods() {
        return resourceMethods;
    }
    
    public List<AbstractSubResourceMethod> getSubResourceMethods() {
        return subResourceMethods;
    }
    
    public List<AbstractSubResourceLocator> getSubResourceLocators() {
        return subResourceLocators;
    }
    
    public AbstractResourceConstructor getConstructor() {
        return ctor;
    }
    
    public List<AbstractResourceIssue> validate() {
        // TODO: add validation logic
        return new LinkedList<AbstractResourceIssue>();
    }
    
    private void printMimeTypes(List<MediaType> mediaTypes, PrintWriter pWriter) {
        boolean firstItem = true;
        for(MediaType mediaType : mediaTypes) {
            if (firstItem) {
                firstItem = false;
            } else {
                pWriter.print(",");
            }
            pWriter.print(mediaType.getType() + "/" + mediaType.getSubtype());
        }
    }
    
    public String toString() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(stringWriter);
        pWriter.println("---Resource java class \"" + this.getResourceClass().getName() + "\"");
        pWriter.println("---path \"" + this.getUriTemplate().getRawTemplate() + "\"");
        for (AbstractResourceMethod rMethod : this.getResourceMethods()) {
            pWriter.println("----method \""  + rMethod.getMethod().getName() + "\"");
            pWriter.println("-----http method \""  + rMethod.getHttpMethod() + "\"");
            pWriter.print("-----consumes: \"");
            printMimeTypes(rMethod.getSupportedInputTypes(), pWriter);
            pWriter.println("\"");
            pWriter.print("-----produces: \"");
            printMimeTypes(rMethod.getSupportedOutputTypes(), pWriter);
            pWriter.println("\"");
        }
        for (AbstractSubResourceMethod rSubResMethod : this.getSubResourceMethods()) {
            pWriter.println("----subresource method \""  + rSubResMethod.getMethod().getName() + "\"");
            pWriter.println("-----path \"" + rSubResMethod.getUriTemplate().getRawTemplate()  + "\"");
            pWriter.println("-----http method \""  + rSubResMethod.getHttpMethod() + "\"");
            pWriter.print("-----consumes: \"");
            printMimeTypes(rSubResMethod.getSupportedInputTypes(), pWriter);
            pWriter.println("\"");
            pWriter.print("-----produces: \"");
            printMimeTypes(rSubResMethod.getSupportedOutputTypes(), pWriter);
            pWriter.println("\"");
        }
        for (AbstractSubResourceLocator rSubResLocator : this.getSubResourceLocators()) {
            pWriter.println("----subresource locator \""  + rSubResLocator.getMethod().getName() + "\"");
            pWriter.println("-----path \"" + rSubResLocator.getUriTemplate().getRawTemplate()  + "\"");
        }
        
        return stringWriter.getBuffer().toString();
    }
}

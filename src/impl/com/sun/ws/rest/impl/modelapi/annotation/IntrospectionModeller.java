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

package com.sun.ws.rest.impl.modelapi.annotation;

import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.api.model.AbstractResourceMethod;
import com.sun.ws.rest.api.model.AbstractWebAppModel;
import com.sun.ws.rest.api.model.AbstractSubResourceLocator;
import com.sun.ws.rest.api.model.AbstractSubResourceMethod;
import com.sun.ws.rest.api.model.UriTemplateValue;
import com.sun.ws.rest.impl.model.*;
import java.lang.reflect.Method;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriTemplate;

/**
 *
 * @author japod
 */
public class IntrospectionModeller {
    
    private static final Logger LOGGER = Logger.getLogger(IntrospectionModeller.class.getName());
    
    // TODO: just leave the code in static methods ?
    public static final AbstractWebAppModel createModel(Set<Class> resourceClasses) {
        
        AbstractWebAppModel model = new AbstractWebAppModel();
        
        // validation is done by resource model validation method
        // so returning an empty resource model is ok
        if (null == resourceClasses) {
            return model;
        }
        
        for (Class<?> resourceClass : resourceClasses) {
            final AbstractResource resource = createResource(resourceClass);
            if (resource.isRootResource()) {
                model.getRootResources().add(resource);
            } else {
                model.getSubResources().add(resource);
            }
        }
        // TODO: what about views ?
        return model;
    }
    
    public static final AbstractResource createResource(Class<?> resourceClass) {
        final UriTemplate rUriTemplateAnnotation = resourceClass.getAnnotation(UriTemplate.class);
        final boolean isRootResourceClass = (null != rUriTemplateAnnotation);
        
        AbstractResource resource;
        
        if (isRootResourceClass) {
            resource = new AbstractResource(resourceClass,
                    new UriTemplateValue(rUriTemplateAnnotation.value(), rUriTemplateAnnotation.encode(), rUriTemplateAnnotation.limited()));
        } else { // just a subresource class
            resource = new AbstractResource(resourceClass);
        }
        
        final MethodList methodList = new MethodList(resourceClass);
        
        final ConsumeMime classScopeConsumeMimeAnnotation = resourceClass.getAnnotation(ConsumeMime.class);
        final ProduceMime classScopeProduceMimeAnnotation = resourceClass.getAnnotation(ProduceMime.class);
        workOutResourceMethodsList(resource, methodList, classScopeConsumeMimeAnnotation, classScopeProduceMimeAnnotation);
        workOutSubResourceMethodsList(resource, methodList, classScopeConsumeMimeAnnotation, classScopeProduceMimeAnnotation);
        workOutSubResourceLocatorsList(resource, methodList);
        
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("A new abstract resource created by IntrospectionModeler: \n" + resource.toString());
        }
        
        return resource;
    }
    
    private static final void findOutConsumeMimeTypes(
            AbstractResourceMethod resourceMethod, ConsumeMime classScopeConsumeMimeAnnotation) {
        
        if (resourceMethod.getMethod().isAnnotationPresent(ConsumeMime.class)){
            final ConsumeMime consumeMimeAnnotation = resourceMethod.getMethod().getAnnotation(ConsumeMime.class);
            resourceMethod.getSupportedInputTypes().addAll(MimeHelper.createMediaTypes(consumeMimeAnnotation));
        } else { // have to use the annotation from class
            resourceMethod.getSupportedInputTypes().addAll(MimeHelper.createMediaTypes(classScopeConsumeMimeAnnotation));
        }
    }
    
    private static final void findOutProduceMimeTypes(
            AbstractResourceMethod resourceMethod, ProduceMime classScopeProduceMimeAnnotation) {
        
        if (resourceMethod.getMethod().isAnnotationPresent(ProduceMime.class)){
            final ProduceMime produceMimeAnnotation = resourceMethod.getMethod().getAnnotation(ProduceMime.class);
            resourceMethod.getSupportedOutputTypes().addAll(MimeHelper.createMediaTypes(produceMimeAnnotation));
        } else { // have to use the annotation from class
            resourceMethod.getSupportedOutputTypes().addAll(MimeHelper.createMediaTypes(classScopeProduceMimeAnnotation));
        }
    }

    
    private static final void workOutResourceMethodsList(
            AbstractResource resource, MethodList methodList, 
            ConsumeMime classScopeConsumeMimeAnnotation, ProduceMime classScopeProduceMimeAnnotation) {
        
        for (Method method : methodList.hasAnnotation(HttpMethod.class).hasNotAnnotation(UriTemplate.class)) {
            
            final HttpMethod httpMethodAnnotation = method.getAnnotation(HttpMethod.class);
            AbstractResourceMethod resourceMethod;
            
            boolean httpMethodAnnotationValueNotNullAndNotEmpty =
                    (null != httpMethodAnnotation.value()) &&  (!"".equals(httpMethodAnnotation.value()));
            
            if (httpMethodAnnotationValueNotNullAndNotEmpty) {
                resourceMethod = new AbstractResourceMethod(method, httpMethodAnnotation.value());
            } else {
                resourceMethod = new AbstractResourceMethod(method);
            }
            
            findOutConsumeMimeTypes(resourceMethod, classScopeConsumeMimeAnnotation);
            findOutProduceMimeTypes(resourceMethod, classScopeProduceMimeAnnotation);

            resource.getResourceMethods().add(resourceMethod);
        }
    }
    
    
    private static final void workOutSubResourceMethodsList(
            AbstractResource resource, MethodList methodList,
            ConsumeMime classScopeConsumeMimeAnnotation, ProduceMime classScopeProduceMimeAnnotation) {
        
        for (Method method : methodList.hasAnnotation(HttpMethod.class).hasAnnotation(UriTemplate.class)) {
            
            final HttpMethod httpMethodAnnotation = method.getAnnotation(HttpMethod.class);
            final UriTemplate mUriTemplateAnnotation = method.getAnnotation(UriTemplate.class);
            AbstractSubResourceMethod subResourceMethod;
            
            if (null != httpMethodAnnotation.value()) {
                subResourceMethod = new AbstractSubResourceMethod(
                        method,
                        new UriTemplateValue(mUriTemplateAnnotation.value(), mUriTemplateAnnotation.encode(), mUriTemplateAnnotation.limited()),
                        httpMethodAnnotation.value());
            } else {
                subResourceMethod = new AbstractSubResourceMethod(method,
                        new UriTemplateValue(mUriTemplateAnnotation.value(), mUriTemplateAnnotation.encode(), mUriTemplateAnnotation.limited()));
            }
            
            findOutConsumeMimeTypes(subResourceMethod, classScopeConsumeMimeAnnotation);
            findOutProduceMimeTypes(subResourceMethod, classScopeProduceMimeAnnotation);

            resource.getSubResourceMethods().add(subResourceMethod);
        }
    }
    
    
    private static final void workOutSubResourceLocatorsList(AbstractResource resource, MethodList methodList) {
        
        for (Method method : methodList.hasNotAnnotation(HttpMethod.class).hasAnnotation(UriTemplate.class)) {
            final UriTemplate mUriTemplateAnnotation = method.getAnnotation(UriTemplate.class);
            final AbstractSubResourceLocator subResourceLocator = new AbstractSubResourceLocator(
                    method,
                    new UriTemplateValue(mUriTemplateAnnotation.value(), mUriTemplateAnnotation.encode(), mUriTemplateAnnotation.limited()));
            
            resource.getSubResourceLocators().add(subResourceLocator);
        }
    }
}

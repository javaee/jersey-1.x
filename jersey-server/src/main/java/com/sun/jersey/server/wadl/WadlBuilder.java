/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.jersey.server.wadl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.server.impl.BuildId;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Doc;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.ParamStyle;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;
import java.util.Collections;

/**
 * This class implements the algorithm how the wadl is built for one or more
 * {@link AbstractResource} classes. Wadl artifacts are created by a
 * {@link WadlGenerator}.
 * Created on: Jun 18, 2008<br>
 * 
 * @author Marc Hadley
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class WadlBuilder {

    private WadlGenerator _wadlGenerator;

    public WadlBuilder() {
        this(new WadlGeneratorImpl());
    }

    public WadlBuilder(WadlGenerator wadlGenerator) {
        _wadlGenerator = wadlGenerator;
    }

    /**
     * Generate WADL for a set of resources.
     * @param resources the set of resources
     * @return the JAXB WADL application bean
     */
    public Application generate(Set<AbstractResource> resources) {
        Application wadlApplication = _wadlGenerator.createApplication();
        Resources wadlResources = _wadlGenerator.createResources();

        // for each resource
        for (AbstractResource r : resources) {
            Resource wadlResource = generateResource(r, null);
            wadlResources.getResource().add(wadlResource);
        }
        wadlApplication.setResources(wadlResources);

        addVersion(wadlApplication);
        return wadlApplication;
    }

    /**
     * Generate WADL for a resource.
     * @param resource the resource
     * @return the JAXB WADL application bean
     */
    public Application generate(AbstractResource resource) {
        Application wadlApplication = _wadlGenerator.createApplication();
        Resources wadlResources = _wadlGenerator.createResources();
        Resource wadlResource = generateResource(resource, null);
        wadlResources.getResource().add(wadlResource);
        wadlApplication.setResources(wadlResources);

        addVersion(wadlApplication);
        return wadlApplication;
    }

    /**
     * Generate WADL for a virtual subresource resulting from sub resource
     * methods.
     * @param resource the parent resource
     * @param path the value of the methods path annotations
     * @return the JAXB WADL application bean
     */
    public Application generate(AbstractResource resource, String path) {
        Application wadlApplication = _wadlGenerator.createApplication();
        Resources wadlResources = _wadlGenerator.createResources();
        Resource wadlResource = generateSubResource(resource, path);
        wadlResources.getResource().add(wadlResource);
        wadlApplication.setResources(wadlResources);

        addVersion(wadlApplication);
        return wadlApplication;
    }

    private void addVersion(Application wadlApplication) {
        // Include Jersey version as doc element with generatedBy attribute
        Doc d = new Doc();
        d.getOtherAttributes().put(new QName("http://jersey.dev.java.net/", "generatedBy", "jersey"),
                BuildId.getBuildId());
        wadlApplication.getDoc().add(0, d);
    }

    private com.sun.research.ws.wadl.Method generateMethod(AbstractResource r, final Map<String, Param> wadlResourceParams, final AbstractResourceMethod m) {
        com.sun.research.ws.wadl.Method wadlMethod = _wadlGenerator.createMethod(r, m);
        // generate the request part
        Request wadlRequest = generateRequest(r, m, wadlResourceParams);
        if (wadlRequest != null) {
            wadlMethod.setRequest(wadlRequest);
        }
        // generate the response part
        Response wadlResponse = generateResponse(r, m);
        if (wadlResponse != null) {
            wadlMethod.setResponse(wadlResponse);
        }
        return wadlMethod;
    }

    private Request generateRequest(AbstractResource r, final AbstractResourceMethod m,
            Map<String, Param> wadlResourceParams) {
        if (m.getParameters().size() == 0) {
            return null;
        }

        Request wadlRequest = _wadlGenerator.createRequest(r, m);

        for (Parameter p : m.getParameters()) {
            if (p.getSource() == Parameter.Source.ENTITY) {
                for (MediaType mediaType : m.getSupportedInputTypes()) {
                    setRepresentationForMediaType(r, m, mediaType, wadlRequest);
                }
            } else if (p.getAnnotation().annotationType() == FormParam.class) {
                // Use application/x-www-form-urlencoded if no @Consumes
                List<MediaType> supportedInputTypes = m.getSupportedInputTypes();
                if (supportedInputTypes.size() == 0
                        || (supportedInputTypes.size() == 1 && supportedInputTypes.get(0).isWildcardType())) {
                    supportedInputTypes = Collections.singletonList(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
                }

                for (MediaType mediaType : supportedInputTypes) {
                    final RepresentationType wadlRepresentation = setRepresentationForMediaType(r, m, mediaType, wadlRequest);
                    if (getParamByName(wadlRepresentation.getParam(), p.getSourceName()) == null) {
                        final Param wadlParam = generateParam(r, m, p);
                        if (wadlParam != null) {
                            wadlRepresentation.getParam().add(wadlParam);
                        }
                    }
                }
            } else {
                Param wadlParam = generateParam(r, m, p);
                if (wadlParam == null) {
                    continue;
                }
                if (wadlParam.getStyle() == ParamStyle.TEMPLATE) {
                    wadlResourceParams.put(wadlParam.getName(), wadlParam);
                } else {
                    wadlRequest.getParam().add(wadlParam);
                }
            }
        }
        if (wadlRequest.getRepresentation().size() + wadlRequest.getParam().size() == 0) {
            return null;
        } else {
            return wadlRequest;
        }
    }

    private Param getParamByName(final List<Param> params, final String name) {
        for (Param param : params) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        return null;
    }

    /**
     * Create the wadl {@link RepresentationType} for the specified {@link MediaType} if not yet
     * existing for the wadl {@link Request} and return it.
     * @param r the resource
     * @param m the resource method
     * @param mediaType an accepted media type of the resource method
     * @param wadlRequest the wadl request the wadl representation is to be created for (if not yet existing).
     * @author Martin Grotzke
     * @return the wadl request representation for the specified {@link MediaType}.
     */
    private RepresentationType setRepresentationForMediaType(AbstractResource r,
            final AbstractResourceMethod m, MediaType mediaType,
            Request wadlRequest) {
        RepresentationType wadlRepresentation = getRepresentationByMediaType(wadlRequest.getRepresentation(), mediaType);
        if (wadlRepresentation == null) {
            wadlRepresentation = _wadlGenerator.createRequestRepresentation(r, m, mediaType);
            wadlRequest.getRepresentation().add(wadlRepresentation);
        }
        return wadlRepresentation;
    }

    private RepresentationType getRepresentationByMediaType(
            final List<RepresentationType> representations, MediaType mediaType) {
        for (RepresentationType representation : representations) {
            if (mediaType.toString().equals(representation.getMediaType())) {
                return representation;
            }
        }
        return null;
    }

    private Param generateParam(AbstractResource r, AbstractMethod m, final Parameter p) {
        if (p.getSource() == Parameter.Source.ENTITY || p.getSource() == Parameter.Source.CONTEXT) {
            return null;
        }
        Param wadlParam = _wadlGenerator.createParam(r, m, p);
        return wadlParam;
    }

    private Resource generateResource(AbstractResource r, String path) {
        return generateResource(r, path, Collections.<Class<?>>emptySet());
    }

    private Resource generateResource(AbstractResource r, String path, Set<Class<?>> visitedClasses) {
        Resource wadlResource = _wadlGenerator.createResource(r, path);

        // prevent infinite recursion
        if (visitedClasses.contains(r.getResourceClass())) {
            return wadlResource;
        } else {
            visitedClasses = new HashSet<Class<?>>(visitedClasses);
            visitedClasses.add(r.getResourceClass());
        }

        // for each resource method
        Map<String, Param> wadlResourceParams = new HashMap<String, Param>();
        for (AbstractResourceMethod m : r.getResourceMethods()) {
            com.sun.research.ws.wadl.Method wadlMethod = generateMethod(r, wadlResourceParams, m);
            wadlResource.getMethodOrResource().add(wadlMethod);
        }
        // add parameters that are associated with the resource PATH template
        for (Param wadlParam : wadlResourceParams.values()) {
            wadlResource.getParam().add(wadlParam);
        }

        // for each sub-resource method
        Map<String, Resource> wadlSubResources = new HashMap<String, Resource>();
        Map<String, Map<String, Param>> wadlSubResourcesParams =
                new HashMap<String, Map<String, Param>>();
        for (AbstractSubResourceMethod m : r.getSubResourceMethods()) {
            // find or create sub resource for uri template
            String template = m.getPath().getValue();
            Resource wadlSubResource = wadlSubResources.get(template);
            Map<String, Param> wadlSubResourceParams = wadlSubResourcesParams.get(template);
            if (wadlSubResource == null) {
                wadlSubResource = new Resource();
                wadlSubResource.setPath(template);
                wadlSubResources.put(template, wadlSubResource);
                wadlSubResourceParams = new HashMap<String, Param>();
                wadlSubResourcesParams.put(template, wadlSubResourceParams);
                wadlResource.getMethodOrResource().add(wadlSubResource);
            }
            com.sun.research.ws.wadl.Method wadlMethod = generateMethod(r, wadlSubResourceParams, m);
            wadlSubResource.getMethodOrResource().add(wadlMethod);
        }
        // add parameters that are associated with each sub-resource method PATH template
        for (Map.Entry<String, Resource> e : wadlSubResources.entrySet()) {
            String template = e.getKey();
            Resource wadlSubResource = e.getValue();
            Map<String, Param> wadlSubResourceParams = wadlSubResourcesParams.get(template);
            for (Param wadlParam : wadlSubResourceParams.values()) {
                wadlSubResource.getParam().add(wadlParam);
            }
        }

        // for each sub resource locator
        for (AbstractSubResourceLocator l : r.getSubResourceLocators()) {
            AbstractResource subResource = IntrospectionModeller.createResource(
                    l.getMethod().getReturnType());
            Resource wadlSubResource = generateResource(subResource,
                    l.getPath().getValue(), visitedClasses);
            wadlResource.getMethodOrResource().add(wadlSubResource);

            for (Parameter p : l.getParameters()) {
                Param wadlParam = generateParam(r, l, p);
                if (wadlParam != null && wadlParam.getStyle() == ParamStyle.TEMPLATE) {
                    wadlSubResource.getParam().add(wadlParam);
                }
            }
        }
        return wadlResource;
    }

    private Resource generateSubResource(AbstractResource r, String path) {
        Resource wadlResource = new Resource();
        if (r.isRootResource()) {
            StringBuilder b = new StringBuilder(r.getPath().getValue());
            if (!(r.getPath().getValue().endsWith("/") || path.startsWith("/"))) {
                b.append("/");
            }
            b.append(path);
            wadlResource.setPath(b.toString());
        }
        // for each sub-resource method
        Map<String, Param> wadlSubResourceParams = new HashMap<String, Param>();
        for (AbstractSubResourceMethod m : r.getSubResourceMethods()) {
            // find or create sub resource for uri template
            String template = m.getPath().getValue();
            if (!template.equals(path)) {
                continue;
            }
            com.sun.research.ws.wadl.Method wadlMethod = generateMethod(r, wadlSubResourceParams, m);
            wadlResource.getMethodOrResource().add(wadlMethod);
        }
        // add parameters that are associated with each sub-resource method PATH template
        for (Param wadlParam : wadlSubResourceParams.values()) {
            wadlResource.getParam().add(wadlParam);
        }

        return wadlResource;
    }

    private Response generateResponse(AbstractResource r, final AbstractResourceMethod m) {
        if (m.getMethod().getReturnType() == void.class) {
            return null;
        }
        Response wadlResponse = _wadlGenerator.createResponse(r, m);
        return wadlResponse;
    }
}

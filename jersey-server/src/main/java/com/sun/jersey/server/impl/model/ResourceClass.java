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

package com.sun.jersey.server.impl.model;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractImplicitViewMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.uri.UriPattern;
import com.sun.jersey.server.impl.model.method.ResourceHeadWrapperMethod;
import com.sun.jersey.server.impl.model.method.ResourceHttpMethod;
import com.sun.jersey.server.impl.model.method.ResourceHttpOptionsMethod;
import com.sun.jersey.server.impl.model.method.ResourceMethod;
import com.sun.jersey.server.impl.uri.rules.HttpMethodRule;
import com.sun.jersey.server.impl.uri.rules.SubLocatorRule;
import com.sun.jersey.server.impl.uri.PathPattern;
import com.sun.jersey.server.impl.uri.PathTemplate;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.api.view.ImplicitProduces;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentInjector;
import com.sun.jersey.server.impl.application.ResourceMethodDispatcherFactory;
import com.sun.jersey.server.impl.template.ViewableRule;
import com.sun.jersey.server.impl.uri.rules.CombiningMatchingPatterns;
import com.sun.jersey.server.impl.uri.rules.PatternRulePair;
import com.sun.jersey.server.impl.uri.rules.RightHandPathRule;
import com.sun.jersey.server.impl.uri.rules.SequentialMatchingPatterns;
import com.sun.jersey.server.impl.uri.rules.TerminatingRule;
import com.sun.jersey.server.impl.uri.rules.UriRulesFactory;
import com.sun.jersey.server.impl.wadl.WadlFactory;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.spi.component.ResourceComponentProvider;
import com.sun.jersey.server.impl.component.ResourceFactory;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.container.filter.FilterFactory;
import com.sun.jersey.server.impl.template.ViewResourceMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HttpMethod;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceClass {
    private final UriRules<UriRule> rules;
    
    private final ResourceConfig config;

    private final WadlFactory wadlFactory;
    
    public final AbstractResource resource;
    
    public ResourceComponentProvider rcProvider;
    
    public ResourceClass(
            ResourceConfig config,
            ResourceMethodDispatcherFactory df,
            ServerInjectableProviderContext injectableContext,
            FilterFactory ff,
            WadlFactory wadlFactory,
            AbstractResource resource
            ) {
        this.resource = resource;

        this.config = config;
        
        this.wadlFactory = wadlFactory;

        final boolean implicitViewables = config.getFeature(
                ResourceConfig.FEATURE_IMPLICIT_VIEWABLES);
        List<QualitySourceMediaType> implictProduces = null;
        if (implicitViewables) {
            ImplicitProduces ip = resource.getAnnotation(ImplicitProduces.class);
            if (ip != null && ip.value() != null && ip.value().length > 0) {
                implictProduces = MediaTypes.createQualitySourceMediaTypes(ip.value());
            }
        }

        RulesMap<UriRule> rulesMap = new RulesMap<UriRule>();

        processSubResourceLocators(ff, injectableContext, rulesMap);

        final Map<PathPattern, ResourceMethodMap> patternMethodMap =
                processSubResourceMethods(implictProduces, df, ff);

        final ResourceMethodMap methodMap = processMethods(implictProduces, df, ff);

        // Create the rules for the sub-resource HTTP methods
        for (Map.Entry<PathPattern, ResourceMethodMap> e : patternMethodMap.entrySet()) {
            final PathPattern p = e.getKey();
            final ResourceMethodMap rmm = e.getValue();
            
            rmm.sort();
            rulesMap.put(p,
                    new RightHandPathRule(
                    config.getFeature(ResourceConfig.FEATURE_REDIRECT),                    
                    p.getTemplate().endsWithSlash(),
                    new HttpMethodRule(rmm, true)));
        }

        // Create the rules for the HTTP methods
        methodMap.sort();
        if (!methodMap.isEmpty()) {
            // No need to adapt with the RightHandPathRule as the URI path
            // will be consumed when such a rule is accepted
            rulesMap.put(PathPattern.EMPTY_PATH, new HttpMethodRule(methodMap));
        }

        
        // Create the atomic rules, at most only one will be matched
        UriRules<UriRule> atomicRules = UriRulesFactory.create(rulesMap);
        
        // Create the end sequential rules, zero or more may be matched
        List<PatternRulePair<UriRule>> patterns = new ArrayList<PatternRulePair<UriRule>>();
        if (config.getFeature(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES)) {
            AbstractImplicitViewMethod method = new AbstractImplicitViewMethod(resource);
            List<ResourceFilter> resourceFilters = ff.getResourceFilters(method);
            ViewableRule r = new ViewableRule(
                    implictProduces,
                    ff.getRequestFilters(resourceFilters),
                    ff.getResponseFilters(resourceFilters));
            ComponentInjector<ViewableRule> ci = new ComponentInjector(injectableContext,
                    ViewableRule.class);
            ci.inject(r);

            // The matching rule for a sub-resource template
            patterns.add(new PatternRulePair<UriRule>(
                    new UriPattern("/([^/]+)"), r));        
            // The matching rule for an index template
            patterns.add(new PatternRulePair<UriRule>(
                    UriPattern.EMPTY, r));                    
        }         
        // The terminating rule when the path is not fully consumed and accepted
        patterns.add(new PatternRulePair<UriRule>(
                new UriPattern(".*"), new TerminatingRule()));
        // The terminating rule when the path is fully consumed and accepted
        patterns.add(new PatternRulePair<UriRule>(
                UriPattern.EMPTY, new TerminatingRule()));        
        // Create the sequential rules
        UriRules<UriRule> sequentialRules = 
                new SequentialMatchingPatterns<UriRule>(patterns);
        
        // Combined the atomic and sequential rules, the former will be matched
        // first
        @SuppressWarnings("unchecked")
        UriRules<UriRule> combiningRules = 
                new CombiningMatchingPatterns<UriRule>(
                Arrays.asList(atomicRules, sequentialRules));
        
        this.rules = combiningRules;
    }
        
    public void init(ResourceFactory rcpFactory) {
        init(rcpFactory, null);
    }

    public void init(ResourceFactory rcpFactory, ComponentContext cc) {
        this.rcProvider = rcpFactory.getComponentProvider(cc, resource.getResourceClass());
        rcProvider.init(resource);
    }

    public void initSingleton(final Object resource) {
        this.rcProvider = new ResourceComponentProvider() {
            public void init(AbstractResource abstractResource) {
            }

            public ComponentScope getScope() {
                return ComponentScope.Singleton;
            }

            public Object getInstance(HttpContext hc) {
                return getInstance();
            }

            public void destroy() {
            }

            public Object getInstance() {
                return resource;
            }
        };
    }

    public void destroy() {
        rcProvider.destroy();
    }

    public UriRules<UriRule> getRules() {
        return rules;
    }

    private void addToPatternMethodMap(
            Map<PathPattern, ResourceMethodMap> tmm,
            PathPattern p,
            ResourceMethod rm) {
        ResourceMethodMap rmm = tmm.get(p);
        if (rmm == null) {
            rmm = new ResourceMethodMap();
            tmm.put(p, rmm);
        }
        rmm.put(rm);
    }

    private void processSubResourceLocators(
            FilterFactory ff,
            ServerInjectableProviderContext injectableContext,
            RulesMap<UriRule> rulesMap) {
        for (final AbstractSubResourceLocator locator : resource.getSubResourceLocators()) {
            UriTemplate t = new PathTemplate(locator.getPath().getValue());
            PathPattern p = new PathPattern(t);

            List<ResourceFilter> resourceFilters = ff.getResourceFilters(locator);
            UriRule r = new SubLocatorRule(
                    t,
                    locator.getMethod(),
                    injectableContext.getInjectable(locator.getParameters(), ComponentScope.PerRequest),
                    ff.getRequestFilters(resourceFilters),
                    ff.getResponseFilters(resourceFilters));

            rulesMap.put(p, 
                    new RightHandPathRule(
                    config.getFeature(ResourceConfig.FEATURE_REDIRECT),
                    t.endsWithSlash(), 
                    r));
        }
    }

    private Map<PathPattern, ResourceMethodMap> processSubResourceMethods(
            List<QualitySourceMediaType> implictProduces,
            ResourceMethodDispatcherFactory df,
            FilterFactory ff) {
        final Map<PathPattern, ResourceMethodMap> patternMethodMap =
                new HashMap<PathPattern, ResourceMethodMap>();
        for (final AbstractSubResourceMethod method : this.resource.getSubResourceMethods()) {

            UriTemplate t = new PathTemplate(method.getPath().getValue());
            PathPattern p = new PathPattern(t, "(/)?");

            ResourceMethod rm = new ResourceHttpMethod(df, ff, t, method);
            addToPatternMethodMap(patternMethodMap, p, rm);
        }

        for (Map.Entry<PathPattern, ResourceMethodMap> e : patternMethodMap.entrySet()) {
            if (implictProduces != null) {
                List<ResourceMethod> getList = e.getValue().get(HttpMethod.GET);
                if (getList != null && !getList.isEmpty()) {
                    e.getValue().put(new ViewResourceMethod(implictProduces));
                }
            }

            processHead(e.getValue());
            processOptions(e.getValue(), this.resource, e.getKey());            
        }

        return patternMethodMap;
    }

    private ResourceMethodMap processMethods(            
            List<QualitySourceMediaType> implictProduces,
            ResourceMethodDispatcherFactory df,
            FilterFactory ff) {
        final ResourceMethodMap methodMap = new ResourceMethodMap();
        for (final AbstractResourceMethod resourceMethod : this.resource.getResourceMethods()) {
            ResourceMethod rm = new ResourceHttpMethod(df, ff, resourceMethod);
            methodMap.put(rm);
        }

        if (implictProduces != null) {
            List<ResourceMethod> getList = methodMap.get(HttpMethod.GET);
            if (getList != null && !getList.isEmpty()) {
                methodMap.put(new ViewResourceMethod(implictProduces));
            }
        }

        processHead(methodMap);
        processOptions(methodMap, this.resource, null);

        return methodMap;
    }

    private void processHead(ResourceMethodMap methodMap) {
        List<ResourceMethod> getList = methodMap.get(HttpMethod.GET);
        if (getList == null || getList.isEmpty()) {
            return;
        }

        List<ResourceMethod> headList = methodMap.get(HttpMethod.HEAD);
        if (headList == null) {
            headList = new ArrayList<ResourceMethod>();
        }

        for (ResourceMethod getMethod : getList) {
            if (!containsMediaOfMethod(headList, getMethod)) {
                ResourceMethod headMethod = new ResourceHeadWrapperMethod(getMethod);
                methodMap.put(headMethod);
                headList = methodMap.get(HttpMethod.HEAD);
            }
        }
    }

    /**
     * Determin if a the resource method list contains a method that 
     * has the same consume/produce media as another resource method.
     * 
     * @param methods the resource methods
     * @param method the resource method to check
     * @return true if the list contains a method with the same media as method.
     */
    private boolean containsMediaOfMethod(List<ResourceMethod> methods,
            ResourceMethod method) {
        for (ResourceMethod m : methods) {
            if (method.mediaEquals(m)) {
                return true;
            }
        }

        return false;
    }

    private void processOptions(ResourceMethodMap methodMap, 
            AbstractResource resource, PathPattern p) {
        List<ResourceMethod> l = methodMap.get("OPTIONS");
        if (l != null) {
            return;
        }

        ResourceMethod optionsMethod = this.wadlFactory.createWadlOptionsMethod(methodMap, resource, p);
        if (optionsMethod == null)
            optionsMethod = new ResourceHttpOptionsMethod(methodMap);
        methodMap.put(optionsMethod);
    }
}
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

package com.sun.jersey.server.impl.model;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractImplicitViewMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.uri.UriPattern;
import com.sun.jersey.api.view.ImplicitProduces;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.header.QualitySourceMediaType;
import com.sun.jersey.core.spi.component.ComponentInjector;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.application.ResourceMethodDispatcherFactory;
import com.sun.jersey.server.impl.container.filter.FilterFactory;
import com.sun.jersey.server.impl.inject.ServerInjectableProviderContext;
import com.sun.jersey.server.impl.model.method.ResourceHeadWrapperMethod;
import com.sun.jersey.server.impl.model.method.ResourceHttpMethod;
import com.sun.jersey.server.impl.model.method.ResourceHttpOptionsMethod;
import com.sun.jersey.server.impl.model.method.ResourceMethod;
import com.sun.jersey.server.impl.template.ViewResourceMethod;
import com.sun.jersey.server.impl.template.ViewableRule;
import com.sun.jersey.server.impl.uri.PathPattern;
import com.sun.jersey.server.impl.uri.PathTemplate;
import com.sun.jersey.server.impl.uri.rules.CombiningMatchingPatterns;
import com.sun.jersey.server.impl.uri.rules.HttpMethodRule;
import com.sun.jersey.server.impl.uri.rules.PatternRulePair;
import com.sun.jersey.server.impl.uri.rules.RightHandPathRule;
import com.sun.jersey.server.impl.uri.rules.SequentialMatchingPatterns;
import com.sun.jersey.server.impl.uri.rules.SubLocatorRule;
import com.sun.jersey.server.impl.uri.rules.TerminatingRule;
import com.sun.jersey.server.impl.uri.rules.UriRulesFactory;
import com.sun.jersey.server.impl.wadl.WadlFactory;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.Errors;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRules;

import javax.ws.rs.HttpMethod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceUriRules {
    private final UriRules<UriRule> rules;

    private final ResourceConfig resourceConfig;

    private final ResourceMethodDispatcherFactory df;

    private final ServerInjectableProviderContext injectableContext;

    private final FilterFactory ff;

    private final WadlFactory wadlFactory;

    public ResourceUriRules(
            final ResourceConfig resourceConfig,
            final ResourceMethodDispatcherFactory df,
            final ServerInjectableProviderContext injectableContext,
            final FilterFactory ff,
            final WadlFactory wadlFactory,
            final AbstractResource resource
            ) {
        this.resourceConfig = resourceConfig;
        this.df = df;
        this.injectableContext = injectableContext;
        this.ff = ff;
        this.wadlFactory = wadlFactory;
        
        final boolean implicitViewables = resourceConfig.getFeature(
                ResourceConfig.FEATURE_IMPLICIT_VIEWABLES);
        List<QualitySourceMediaType> implictProduces = null;
        if (implicitViewables) {
            ImplicitProduces ip = resource.getAnnotation(ImplicitProduces.class);
            if (ip != null && ip.value() != null && ip.value().length > 0) {
                implictProduces = MediaTypes.createQualitySourceMediaTypes(ip.value());
            }
        }

        RulesMap<UriRule> rulesMap = new RulesMap<UriRule>();

        processSubResourceLocators(resource, rulesMap);

        processSubResourceMethods(resource, implictProduces, rulesMap);

        processMethods(resource, implictProduces, rulesMap);

        // Check for matching conflicts with path patterns
        rulesMap.processConflicts(new RulesMap.ConflictClosure() {
            public void onConflict(PathPattern p1, PathPattern p2) {
                Errors.error(String.format("Conflicting URI templates. "
                        + "The URI templates %s and %s for sub-resource methods "
                        + "and/or sub-resource locators of resource class %s "
                        + "transform to the same regular expression %s",
                        p1.getTemplate().getTemplate(),
                        p2.getTemplate().getTemplate(),
                        resource.getResourceClass().getName(),
                        p1));
            }
        });

        // Create the atomic rules, at most only one will be matched
        final UriRules<UriRule> atomicRules = UriRulesFactory.create(rulesMap);
        
        // Create the end sequential rules, zero or more may be matched
        List<PatternRulePair<UriRule>> patterns = new ArrayList<PatternRulePair<UriRule>>();
        if (resourceConfig.getFeature(ResourceConfig.FEATURE_IMPLICIT_VIEWABLES)) {
            AbstractImplicitViewMethod method = new AbstractImplicitViewMethod(resource);
            List<ResourceFilter> resourceFilters = ff.getResourceFilters(method);
            ViewableRule r = new ViewableRule(
                    implictProduces,
                    FilterFactory.getRequestFilters(resourceFilters),
                    FilterFactory.getResponseFilters(resourceFilters));
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
        final UriRules<UriRule> sequentialRules =
                new SequentialMatchingPatterns<UriRule>(patterns);
        
        // Combined the atomic and sequential rules, the former will be matched
        // first
        final UriRules<UriRule> combiningRules =
                new CombiningMatchingPatterns<UriRule>(
                Arrays.asList(atomicRules, sequentialRules));
        
        this.rules = combiningRules;
    }
        
    public UriRules<UriRule> getRules() {
        return rules;
    }

    private void processSubResourceLocators(
            final AbstractResource resource,
            final RulesMap<UriRule> rulesMap) {
        for (final AbstractSubResourceLocator locator : resource.getSubResourceLocators()) {
            PathPattern p = null;
            try {
                p = new PathPattern(new PathTemplate(locator.getPath().getValue()));
            } catch (IllegalArgumentException ex) {
                Errors.error(String.format("Illegal URI template for sub-resource locator %s: %s",
                        locator.getMethod(), ex.getMessage()));
                continue;
            }

            final PathPattern conflict = rulesMap.hasConflict(p);
            if (conflict != null) {
                Errors.error(String.format("Conflicting URI templates. "
                        + "The URI template %s for sub-resource locator %s "
                        + "and the URI template %s transform to the same regular expression %s",
                        p.getTemplate().getTemplate(),
                        locator.getMethod(),
                        conflict.getTemplate().getTemplate(),
                        p));
                continue;
            }

            final List<Injectable> is = injectableContext.getInjectable(locator.getParameters(), ComponentScope.PerRequest);
            if (is.contains(null)) {
                // Missing dependency
                for (int i = 0; i < is.size(); i++) {
                    if (is.get(i) == null) {
                        Errors.missingDependency(locator.getMethod(), i);
                    }
                }
            }
            
            final List<ResourceFilter> resourceFilters = ff.getResourceFilters(locator);
            final UriRule r = new SubLocatorRule(
                    p.getTemplate(),
                    locator.getMethod(),
                    is,
                    FilterFactory.getRequestFilters(resourceFilters),
                    FilterFactory.getResponseFilters(resourceFilters));

            rulesMap.put(p, 
                    new RightHandPathRule(
                    resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                    p.getTemplate().endsWithSlash(),
                    r));
        }
    }

    private void processSubResourceMethods(
            final AbstractResource resource,
            final List<QualitySourceMediaType> implictProduces,
            final RulesMap<UriRule> rulesMap) {
        final Map<PathPattern, ResourceMethodMap> patternMethodMap =
                new HashMap<PathPattern, ResourceMethodMap>();

        for (final AbstractSubResourceMethod method : resource.getSubResourceMethods()) {

            PathPattern p = null;
            try {
                p = new PathPattern(new PathTemplate(method.getPath().getValue()), "(/)?");
            } catch (IllegalArgumentException ex) {
                Errors.error(String.format("Illegal URI template for sub-resource method %s: %s",
                        method.getMethod(), ex.getMessage()));
                continue;
            }

            final ResourceMethod rm = new ResourceHttpMethod(df, ff, p.getTemplate(), method);
            ResourceMethodMap rmm = patternMethodMap.get(p);
            if (rmm == null) {
                rmm = new ResourceMethodMap();
                patternMethodMap.put(p, rmm);
            }

            if (isValidResourceMethod(rm, rmm)) {
                rmm.put(rm);
            }
         
            rmm.put(rm);
        }

        // Create the rules for the sub-resource HTTP methods
        for (final Map.Entry<PathPattern, ResourceMethodMap> e : patternMethodMap.entrySet()) {
            addImplicitMethod(implictProduces, e.getValue());

            final PathPattern p = e.getKey();
            final ResourceMethodMap rmm = e.getValue();

            processHead(rmm);
            processOptions(rmm, resource, p);

            rmm.sort();
            
            rulesMap.put(p,
                    new RightHandPathRule(
                    resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                    p.getTemplate().endsWithSlash(),
                    new HttpMethodRule(rmm, true)));
        }
    }

    private void processMethods(
            final AbstractResource resource,
            final List<QualitySourceMediaType> implictProduces,
            final RulesMap<UriRule> rulesMap) {
        final ResourceMethodMap rmm = new ResourceMethodMap();
        for (final AbstractResourceMethod resourceMethod : resource.getResourceMethods()) {
            ResourceMethod rm = new ResourceHttpMethod(df, ff, resourceMethod);

            if (isValidResourceMethod(rm, rmm)) {
                rmm.put(rm);
            }
        }

        addImplicitMethod(implictProduces, rmm);

        processHead(rmm);
        processOptions(rmm, resource, null);

        // Create the rules for the HTTP methods
        rmm.sort();
        if (!rmm.isEmpty()) {
            // No need to adapt with the RightHandPathRule as the URI path
            // will be consumed when such a rule is accepted
            rulesMap.put(PathPattern.EMPTY_PATH, new HttpMethodRule(rmm));
        }
    }

    private void addImplicitMethod(
            final List<QualitySourceMediaType> implictProduces,
            final ResourceMethodMap rmm) {
        if (implictProduces != null) {
            final List<ResourceMethod> getList = rmm.get(HttpMethod.GET);
            if (getList != null && !getList.isEmpty()) {
                rmm.put(new ViewResourceMethod(implictProduces));
            }
        }
    }

    private boolean isValidResourceMethod(
            final ResourceMethod rm,
            final ResourceMethodMap rmm) {
        final List<ResourceMethod> rml = rmm.get(rm.getHttpMethod());
        if (rml != null) {
            boolean conflict = false;
            ResourceMethod erm = null;
            for (int i = 0; i < rml.size() && !conflict; i++) {
                erm = rml.get(i);

                conflict = MediaTypes.intersects(rm.getConsumes(), erm.getConsumes())
                        && MediaTypes.intersects(rm.getProduces(), erm.getProduces());
            }

            if (conflict) {
                if (rm.getAbstractResourceMethod().hasEntity()) {
                    Errors.error(String.format("Consuming media type conflict. " +
                            "The resource methods %s and %s can consume the same media type",
                            rm.getAbstractResourceMethod().getMethod(), erm.getAbstractResourceMethod().getMethod()));
                } else {
                    Errors.error(String.format("Producing media type conflict. " +
                            "The resource methods %s and %s can produce the same media type",
                            rm.getAbstractResourceMethod().getMethod(), erm.getAbstractResourceMethod().getMethod()));
                }
            }

            if (conflict)
                return false;
        }

        return true;
    }

    private void processHead(final ResourceMethodMap methodMap) {
        final List<ResourceMethod> getList = methodMap.get(HttpMethod.GET);
        if (getList == null || getList.isEmpty()) {
            return;
        }

        List<ResourceMethod> headList = methodMap.get(HttpMethod.HEAD);
        if (headList == null) {
            headList = new ArrayList<ResourceMethod>();
        }

        for (final ResourceMethod getMethod : getList) {
            if (!containsMediaOfMethod(headList, getMethod)) {
                final ResourceMethod headMethod = new ResourceHeadWrapperMethod(getMethod);
                methodMap.put(headMethod);
                headList = methodMap.get(HttpMethod.HEAD);
            }
        }
    }

    /**
     * Determine if a the resource method list contains a method that
     * has the same consume/produce media as another resource method.
     * 
     * @param methods the resource methods
     * @param method the resource method to check
     * @return true if the list contains a method with the same media as method.
     */
    private boolean containsMediaOfMethod(
            final List<ResourceMethod> methods,
            final ResourceMethod method) {
        for (final ResourceMethod m : methods) {
            if (method.mediaEquals(m)) {
                return true;
            }
        }

        return false;
    }

    private void processOptions(
            final ResourceMethodMap methodMap,
            final AbstractResource resource,
            final PathPattern p) {
        final List<ResourceMethod> l = methodMap.get("OPTIONS");
        if (l != null) {
            return;
        }

        ResourceMethod optionsMethod = wadlFactory.createWadlOptionsMethod(methodMap, resource, p);
        if (optionsMethod == null) {
            optionsMethod = new ResourceHttpOptionsMethod(methodMap);
        }
        methodMap.put(optionsMethod);
    }
}
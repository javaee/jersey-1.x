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

package com.sun.ws.rest.impl.model;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.uri.UriPattern;
import com.sun.ws.rest.impl.model.method.ResourceHeadWrapperMethod;
import com.sun.ws.rest.impl.model.method.ResourceHttpMethod;
import com.sun.ws.rest.impl.model.method.ResourceHttpOptionsMethod;
import com.sun.ws.rest.impl.model.method.ResourceMethod;
import com.sun.ws.rest.impl.model.parameter.ParameterExtractorFactory;
import com.sun.ws.rest.impl.uri.rules.HttpMethodRule;
import com.sun.ws.rest.impl.uri.rules.SubLocatorRule;
import com.sun.ws.rest.impl.uri.PathPattern;
import com.sun.ws.rest.impl.uri.PathTemplate;
import com.sun.jersey.api.uri.UriTemplate;
import com.sun.ws.rest.impl.template.ViewableRule;
import com.sun.ws.rest.impl.uri.rules.CombiningMatchingPatterns;
import com.sun.ws.rest.impl.uri.rules.PatternRulePair;
import com.sun.ws.rest.impl.uri.rules.RightHandPathRule;
import com.sun.ws.rest.impl.uri.rules.SequentialMatchingPatterns;
import com.sun.ws.rest.impl.uri.rules.TerminatingRule;
import com.sun.ws.rest.impl.uri.rules.UriRulesFactory;
import com.sun.ws.rest.impl.wadl.WadlFactory;
import com.sun.jersey.spi.resource.ResourceProvider;
import com.sun.jersey.spi.resource.ResourceProviderFactory;
import com.sun.jersey.spi.service.ComponentProvider;
import com.sun.jersey.spi.uri.rules.UriRule;
import com.sun.jersey.spi.uri.rules.UriRules;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.ws.rs.HttpMethod;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ResourceClass {
    private static final Logger LOGGER = Logger.getLogger(ResourceClass.class.getName());
    
    private final UriRules<UriRule> rules;
    
    private final ResourceConfig config;
    
    public final AbstractResource resource;
    
    public final ResourceProvider resolver;
    
    public ResourceClass(ResourceConfig config,
            ComponentProvider provider,
            ResourceProviderFactory resolverFactory, 
            AbstractResource resource) {
        this.resource = resource;

        this.config = config;

        this.resolver = resolverFactory.createProvider(
                provider, resource, 
                config.getFeatures(), config.getProperties());

        RulesMap<UriRule> rulesMap = new RulesMap<UriRule>();

        processSubResourceLocators(rulesMap);

        final Map<PathPattern, ResourceMethodMap> patternMethodMap =
                processSubResourceMethods();

        final ResourceMethodMap methodMap = processMethods();

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
            UriRule r = new ViewableRule();
            provider.inject(r);
            // The matching rule for a sub-resource template
            patterns.add(new PatternRulePair<UriRule>(
                    new UriPattern("/([^/]+)"), r));        
            // The matching rule for an index template
            patterns.add(new PatternRulePair<UriRule>(
                    new UriPattern(null), r));                    
        }         
        // The terminating rule when the path is not fully consumed and accepted
        patterns.add(new PatternRulePair<UriRule>(
                new UriPattern(".*"), new TerminatingRule()));
        // The terminating rule when the path is fully consumed and accepted
        patterns.add(new PatternRulePair<UriRule>(
                new UriPattern(null), new TerminatingRule()));        
        // Create the sequential rules
        UriRules<UriRule> sequentialRules = 
                new SequentialMatchingPatterns<UriRule>(patterns);
        
        // Combined the atomic and sequential rules, the former will be matched
        // first
        @SuppressWarnings("unchecked")
        UriRules<UriRule> combiningRules = 
                new CombiningMatchingPatterns<UriRule>(
                Arrays.asList(atomicRules, sequentialRules));
        
        // this.rules = UriRulesFactory.create(rulesMap);
        this.rules = combiningRules;
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

    private void processSubResourceLocators(RulesMap<UriRule> rulesMap) {
        for (final AbstractSubResourceLocator locator : resource.getSubResourceLocators()) {
            UriTemplate t = new PathTemplate(
                    locator.getUriPath().getValue(),
                    locator.getUriPath().isEncode());

            PathPattern p = new PathPattern(
                    t,
                    locator.getUriPath().isLimited());

            UriRule r = new SubLocatorRule(
                    t,
                    locator.getMethod(),
                    ParameterExtractorFactory.createExtractorsForSublocator(locator));

            rulesMap.put(p, 
                    new RightHandPathRule(
                    config.getFeature(ResourceConfig.FEATURE_REDIRECT),
                    t.endsWithSlash(), 
                    r));
        }
    }

    private Map<PathPattern, ResourceMethodMap> processSubResourceMethods() {
        final Map<PathPattern, ResourceMethodMap> patternMethodMap =
                new HashMap<PathPattern, ResourceMethodMap>();
        for (final AbstractSubResourceMethod method : this.resource.getSubResourceMethods()) {

            UriTemplate t = new PathTemplate(
                    method.getUriPath().getValue(),
                    method.getUriPath().isEncode());

            PathPattern p = new PathPattern(t,  method.getUriPath().isLimited());

            ResourceMethod rm = new ResourceHttpMethod(t, method);
            addToPatternMethodMap(patternMethodMap, p, rm);
        }

        for (Map.Entry<PathPattern, ResourceMethodMap> e : patternMethodMap.entrySet()) {
            processHead(e.getValue());
            processOptions(e.getValue(), this.resource, e.getKey());            
        }

        return patternMethodMap;
    }

    private ResourceMethodMap processMethods() {
        final ResourceMethodMap methodMap = new ResourceMethodMap();
        for (final AbstractResourceMethod resourceMethod : this.resource.getResourceMethods()) {
            ResourceMethod rm = new ResourceHttpMethod(resourceMethod);
            methodMap.put(rm);
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

        ResourceMethod optionsMethod = WadlFactory.createWadlOptionsMethod(methodMap, resource, p);
        if (optionsMethod == null)
            optionsMethod = new ResourceHttpOptionsMethod(methodMap);
        methodMap.put(optionsMethod);
    }
}
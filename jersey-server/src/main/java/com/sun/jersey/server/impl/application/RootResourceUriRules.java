/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.server.impl.application;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.core.spi.component.ComponentInjector;
import com.sun.jersey.core.spi.factory.InjectableProviderFactory;
import com.sun.jersey.impl.ImplMessages;
import com.sun.jersey.server.impl.model.RulesMap;
import com.sun.jersey.server.impl.uri.PathPattern;
import com.sun.jersey.server.impl.uri.PathTemplate;
import com.sun.jersey.server.impl.uri.rules.ResourceClassRule;
import com.sun.jersey.server.impl.uri.rules.ResourceObjectRule;
import com.sun.jersey.server.impl.uri.rules.RightHandPathRule;
import com.sun.jersey.server.impl.wadl.WadlFactory;
import com.sun.jersey.server.impl.wadl.WadlResource;
import com.sun.jersey.spi.inject.Errors;
import com.sun.jersey.spi.uri.rules.UriRule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * A class that creates a rules map for root resources.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class RootResourceUriRules {
    private static final Logger LOGGER = Logger.getLogger(RootResourceUriRules.class.getName());

    private final RulesMap<UriRule> rules = new RulesMap<UriRule>();

    private final WebApplicationImpl wa;

    private final WadlFactory wadlFactory;

    private final ResourceConfig resourceConfig;

    private final InjectableProviderFactory injectableFactory;

    public RootResourceUriRules(
            final WebApplicationImpl wa,
            final ResourceConfig resourceConfig,
            final WadlFactory wadlFactory,
            final InjectableProviderFactory injectableFactory) {
        this.wa = wa;
        this.resourceConfig = resourceConfig;
        this.wadlFactory = wadlFactory;
        this.injectableFactory = injectableFactory;

        final Set<Class<?>> classes = resourceConfig.getRootResourceClasses();

        final Set<Object> singletons = resourceConfig.getRootResourceSingletons();

        if (classes.isEmpty() &&
                singletons.isEmpty() &&
                resourceConfig.getExplicitRootResources().isEmpty()) {
            LOGGER.severe(ImplMessages.NO_ROOT_RES_IN_RES_CFG());
            throw new ContainerException(ImplMessages.NO_ROOT_RES_IN_RES_CFG());
        }


        final Set<AbstractResource> rootResourcesSet =
                new HashSet<AbstractResource>();

        // Add declared singleton instances of root resource classes
        for (final Object o : singletons) {
            rootResourcesSet.add(wa.getAbstractResource(o));
        }

        // Add declared root resource classes
        for (final Class<?> c : classes) {
            rootResourcesSet.add(wa.getAbstractResource(c));
        }

        // Add explicit declared root resource classes
        final Map<String, AbstractResource> explicitRootResources =
                new HashMap<String, AbstractResource>();
        for (final Map.Entry<String, Object> e : resourceConfig.getExplicitRootResources().entrySet()) {
            final Object o = e.getValue();
            final Class c = (o instanceof Class) ? (Class)o : o.getClass();

            final AbstractResource ar = new AbstractResource(e.getKey(),
                    wa.getAbstractResource(c));

            rootResourcesSet.add(ar);
            explicitRootResources.put(e.getKey(), ar);
        }


        // Initiate the WADL with the root resources
        initWadl(rootResourcesSet);


        // Process singleton instances of root resource classes
        for (final Object o : singletons) {
            final AbstractResource ar = wa.getAbstractResource(o);
            // Configure meta-data
            wa.initiateResource(ar, o);
            
            final ComponentInjector ci = new ComponentInjector(injectableFactory, o.getClass());
            ci.inject(o);

            addRule(ar.getPath().getValue(), o);
        }

        // Process root resource classes
        for (final Class<?> c : classes) {
            final AbstractResource ar = wa.getAbstractResource(c);
            // Configure meta-data
            wa.initiateResource(ar);

            addRule(ar.getPath().getValue(), c);
        }

        // Process explicit root resources
        for (final Map.Entry<String, Object> e : resourceConfig.getExplicitRootResources().entrySet()) {
            final String path = e.getKey();
            final Object o = e.getValue();
            if (o instanceof Class) {
                final Class c = (Class)o;
                
                // Configure meta-data
                wa.initiateResource(explicitRootResources.get(path));

                addRule(path, c);
            } else {
                // Configure meta-data
                wa.initiateResource(explicitRootResources.get(path));

                final ComponentInjector ci = new ComponentInjector(injectableFactory, o.getClass());
                ci.inject(o);

                addRule(path, o);
            }
        }

        rules.processConflicts(new RulesMap.ConflictClosure() {
            public void onConflict(PathPattern p1, PathPattern p2) {
                Errors.error(String.format("Conflicting URI templates. "
                        + "The URI templates %s and %s for root resource classes "
                        + "transform to the same regular expression %s",
                        p1.getTemplate().getTemplate(),
                        p2.getTemplate().getTemplate(),
                        p1));
            }
        });

        initWadlResource();
    }

    private void initWadl(final Set<AbstractResource> rootResources) {
        if (!wadlFactory.isSupported())
            return;

        wadlFactory.init(injectableFactory, rootResources);
    }

    private void initWadlResource() {
        if (!wadlFactory.isSupported())
            return;

        final PathPattern p = new PathPattern(new PathTemplate("application.wadl"));

        // If "application.wadl" is already defined to not add the
        // default WADL resource
        if (rules.containsKey(p))
            return;

        // Configure meta-data
        wa.initiateResource(WadlResource.class);

        rules.put(p, new RightHandPathRule(
                resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                p.getTemplate().endsWithSlash(),
                new ResourceClassRule(p.getTemplate(), WadlResource.class)));
    }

    private void addRule(final String path, final Class c) {
        final PathPattern p = getPattern(path, c);
        if (isPatternValid(p, c)) {
            rules.put(p, new RightHandPathRule(
                        resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                        p.getTemplate().endsWithSlash(),
                        new ResourceClassRule(p.getTemplate(), c)));
        }
    }

    private void addRule(final String path, final Object o) {
        final PathPattern p = getPattern(path, o.getClass());
        if (isPatternValid(p, o.getClass())) {
            rules.put(p, new RightHandPathRule(
                        resourceConfig.getFeature(ResourceConfig.FEATURE_REDIRECT),
                        p.getTemplate().endsWithSlash(),
                        new ResourceObjectRule(p.getTemplate(), o)));
        }
    }

    private PathPattern getPattern(String path, Class c) {
        PathPattern p = null;
        try {
            p = new PathPattern(new PathTemplate(path));
        } catch (IllegalArgumentException ex) {
            Errors.error("Illegal URI template for root resource class " + c.getName() +
                    ": "+ ex.getMessage());
        }
        return p;
    }

    private boolean isPatternValid(PathPattern p, Class c) {
        if (p == null)
            return false;

        final PathPattern conflict = rules.hasConflict(p);
        if (conflict != null) {
            Errors.error(String.format("Conflicting URI templates. "
                    + "The URI template %s for root resource class %s "
                    + "and the URI template %s transform to the same regular expression %s",
                    p.getTemplate().getTemplate(),
                    c.getName(),
                    conflict.getTemplate().getTemplate(),
                    p));
            return false;
        } else {
            return true;
        }
    }

    public RulesMap<UriRule> getRules() {
        return rules;
    }
}

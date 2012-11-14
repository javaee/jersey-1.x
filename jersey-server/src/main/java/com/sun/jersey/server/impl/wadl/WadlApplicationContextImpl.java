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
package com.sun.jersey.server.impl.wadl;

import java.net.URI;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.wadl.config.WadlGeneratorConfig;
import com.sun.jersey.api.wadl.config.WadlGeneratorConfigLoader;
import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.WadlApplicationContext;
import com.sun.jersey.server.wadl.WadlBuilder;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Doc;
import com.sun.research.ws.wadl.Grammars;
import com.sun.research.ws.wadl.Include;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class WadlApplicationContextImpl implements WadlApplicationContext {

    private static final Logger LOG = Logger.getLogger(WadlApplicationContextImpl.class.getName());
    private boolean wadlGenerationEnabled = true;
    private final Set<AbstractResource> rootResources;
    private final WadlGeneratorConfig wadlGeneratorConfig;
    private JAXBContext jaxbContext;

    public WadlApplicationContextImpl(
            Set<AbstractResource> rootResources,
            ResourceConfig resourceConfig) {
        this.rootResources = rootResources;
        this.wadlGeneratorConfig = WadlGeneratorConfigLoader.loadWadlGeneratorsFromConfig(resourceConfig);

        try {
            // TODO perhaps this should be done another way for the moment
            // create a temporary generator just to do this one task
            final WadlGenerator wadlGenerator = this.wadlGeneratorConfig.createWadlGenerator();
            final String requiredJaxbContextPath = wadlGenerator.getRequiredJaxbContextPath();

            this.jaxbContext = null;
            try {
                // the following works fine in WLS and non-GF environment
                this.jaxbContext = JAXBContext.newInstance(requiredJaxbContextPath, wadlGenerator.getClass().getClassLoader());
            } catch (JAXBException ex) {
                // fallback for GF
                LOG.log(Level.WARNING, ex.getMessage(), ex);
                this.jaxbContext = JAXBContext.newInstance(requiredJaxbContextPath);
            }
        } catch (JAXBException ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

//    public ApplicationDescription getApplication() {
//        return getWadlBuilder().generate(rootResources);
//    }
    @Override
    public ApplicationDescription getApplication(UriInfo uriInfo) {
        ApplicationDescription a = getWadlBuilder().generate(uriInfo, rootResources);
        final Application application = a.getApplication();
        for (Resources resources : application.getResources()) {
            if (resources.getBase() == null) {
                resources.setBase(uriInfo.getBaseUri().toString());
            }
        }
        attachExternalGrammar(application, a, uriInfo.getRequestUri());
        return a;
    }

    @Override
    public Application getApplication(UriInfo info,
            AbstractResource resource,
            String path) {

        // Get the root application description
        //

        ApplicationDescription description = getApplication(info);

        WadlGenerator wadlGenerator = wadlGeneratorConfig.createWadlGenerator();

        Application a = path == null ? new WadlBuilder( wadlGenerator ).generate(info, description,resource) :
                new WadlBuilder( wadlGenerator ).generate(info, description, resource, path);

        for (Resources resources : a.getResources()) {
            resources.setBase(info.getBaseUri().toString());
        }

        // Attach any grammar we may have

        attachExternalGrammar(a, description,
                info.getRequestUri());

        for (Resources resources : a.getResources()) {
            final Resource r = resources.getResource().get(0);
            r.setPath(info.getBaseUri().relativize(info.getAbsolutePath()).toString());

            // remove path params since path is fixed at this point
            r.getParam().clear();
        }

        return a;
    }

    /**
     * @TODO probably no longer required
     * @return
     */
    @Override
    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }

    private WadlBuilder getWadlBuilder() {
        return (this.wadlGenerationEnabled ? new WadlBuilder(wadlGeneratorConfig.createWadlGenerator()) : null);
    }

    @Override
    public void setWadlGenerationEnabled(boolean wadlGenerationEnabled) {
        this.wadlGenerationEnabled = wadlGenerationEnabled;
    }

    @Override
    public boolean isWadlGenerationEnabled() {
        return wadlGenerationEnabled;
    }

    /**
     * Update the application object to include the generated grammar objects
     */
    private void attachExternalGrammar(
            Application application,
            ApplicationDescription applicationDescription,
            URI requestURI) {

        // Massage the application.wadl URI slightly to get the right effect
        //

        final String requestURIPath = requestURI.getPath();

        if (requestURIPath.endsWith("application.wadl")) {
            requestURI = UriBuilder.fromUri(requestURI)
                    .replacePath(
                    requestURIPath
                    .substring(0, requestURIPath.lastIndexOf('/') + 1))
                    .build();
        }


        String root = application.getResources().get(0).getBase();
        UriBuilder extendedPath = root != null
                ? UriBuilder.fromPath(root).path("/application.wadl/")
                : UriBuilder.fromPath("./application.wadl/");
        URI rootURI = root != null ? UriBuilder.fromPath(root).build() : null;


        // Add a reference to this grammar
        //

        Grammars grammars;
        if (application.getGrammars() != null) {
            LOG.info("The wadl application already contains a grammars element,"
                    + " we're adding elements of the provided grammars file.");
            grammars = application.getGrammars();
        } else {
            grammars = new Grammars();
            application.setGrammars(grammars);
        }

        // Create a reference back to the root WADL
        //

        for (String path : applicationDescription.getExternalMetadataKeys()) {
            ApplicationDescription.ExternalGrammar eg =
                    applicationDescription.getExternalGrammar(path);
            
            if (!eg.isIncludedInGrammar()) {
                continue;
            }
            
            URI schemaURI =
                    extendedPath.clone().path(path).build();

            String schemaURIS = schemaURI.toString();
            String requestURIs = requestURI.toString();

            String schemaPath = rootURI != null
                    ? requestURI.relativize(schemaURI).toString()
                    : schemaURI.toString();

            Include include = new Include();
            include.setHref(schemaPath);
            Doc doc = new Doc();
            doc.setLang("en");
            doc.setTitle("Generated");
            include.getDoc().add(doc);

            // Finally add to list
            grammars.getInclude().add(include);
        }
    }
}

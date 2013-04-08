/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.server.wadl;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.util.FeaturesAndProperties;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;


import javax.ws.rs.ext.Providers;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.UriInfo;


/**
 * A WadlGenerator creates artifacts related to wadl. This is designed as an interface,
 * so that several implementations can decorate existing ones. One decorator could e.g. add
 * references to definitions within some xsd for existing representations.<br>
 * Created on: Jun 16, 2008<br>
 *
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public interface WadlGenerator {

    /**
     * Sets the delegate that is decorated by this wadl generator. Is invoked directly after
     * this generator is instantiated before {@link #init()} or any setter method is invoked.
     * @param delegate the wadl generator to decorate
     */
    void setWadlGeneratorDelegate( WadlGenerator delegate );

    /**
     * Invoked before all methods related to wadl-building are invoked. This method is used in a
     * decorator like manner, and therefore has to invoke <code>this.delegate.init()</code>.
     * @throws Exception
     */
    void init() throws Exception;

    /**
     * The jaxb context path that is used when the generated wadl application is marshalled
     * to a file.<br/>
     * This method is used in a decorator like manner.<br/>
     * The result return the path (or a colon-separated list of package names) containing
     * jaxb-beans that are added to wadl elements by this WadlGenerator, additionally to
     * the context path of the decorated WadlGenerator (set by {@link #setWadlGeneratorDelegate(WadlGenerator)}.<br/>
     * If you do not use custom jaxb beans, then simply return <code>_delegate.getRequiredJaxbContextPath()</code>,
     * otherwise return the delegate's {@link #getRequiredJaxbContextPath()} together with
     * your required context path (separated by a colon):<br/>
     * <pre><code>_delegate.getRequiredJaxbContextPath() == null
     ? ${yourContextPath}
     : _delegate.getRequiredJaxbContextPath() + ":" + ${yourContextPath};</code></pre>
     *
     * If you add the path for your custom jaxb beans, don't forget to add an
     * ObjectFactory (annotated with {@link XmlRegistry}) to this package.
     * @return simply the {@link #getRequiredJaxbContextPath()} of the delegate or the
     *  {@link #getRequiredJaxbContextPath()} + ":" + ${yourContextPath}.
     */
    String getRequiredJaxbContextPath();
    
    
    /**
     * A method parameter to make it easier to supply more environmental
     * information later without break the existing API.
     */
    public class Environment
    {
        private Providers providers;
        private FeaturesAndProperties fap;
        
        public Environment setProviders(Providers providers)
        {
            this.providers = providers;
            return this;
        }
        
        public Providers getProviders() { return providers; }
        
        public Environment setFeaturesAndProperties(FeaturesAndProperties fap)
        {
            this.fap = fap;
            return this;
        }
        
        public FeaturesAndProperties getFeaturesAndProperties() { return fap; }
    }

    /**
     * Provides the WadlGenerator with the current generating environment.
     * This method is used in a decorator like manner, and therefore has to 
     * invoke <code>this.delegate.setEnvironment(env)</code>.
     */
    public void setEnvironment(Environment env);

    // ================  methods for building the wadl application =============

    public Application createApplication(UriInfo requestInfo);

    public Resources createResources();

    public Resource createResource(AbstractResource r,
                                   String path);

    public com.sun.research.ws.wadl.Method createMethod(AbstractResource r,
                                                        AbstractResourceMethod m);

    public Request createRequest(AbstractResource r,
                                 AbstractResourceMethod m);

    public Representation createRequestRepresentation(AbstractResource r,
                                                      AbstractResourceMethod m,
                                                      MediaType mediaType);

    public List<Response> createResponses(AbstractResource r,
                                          AbstractResourceMethod m);

    public Param createParam(AbstractResource r,
                             AbstractMethod m,
                             Parameter p);

    // ================ methods for post build actions =======================

    /**
     * Call back interface that the create external grammar can use
     * to allow other parts of the code to attach the correct grammar information
     */
    public interface Resolver
    {
        /**
         * @param resolvedType The type of the element to result, for XML Schema
         *   this is going to be QName, for JSON Schema URI. If the resolver
         *   doesn't recognise this type then it should return null.
         * @param wadlType The type of the class
         * @param mt The media type it needs to be resolve relative to.
         * @return The schema type of the class if defined, null if not.
         */
        public <T> T resolve(Class wadlType, MediaType mt, Class<T> resolvedType);
    }

    /**
     * And internal storage object to store the grammar definitions and 
     * any type resolvers that are created along the way.
     */
    public class ExternalGrammarDefinition {

        // final public field to make a property was thinking about encapsulation
        // but decided code much simpler without
        public final Map<String, ApplicationDescription.ExternalGrammar>
                map = new LinkedHashMap<String, ApplicationDescription.ExternalGrammar>();

        private List<Resolver> typeResolvers = new ArrayList<Resolver>();

        public void addResolver(Resolver resolver) {
            assert !typeResolvers.contains(resolver) : "Already in list";
            typeResolvers.add(0,resolver);
        }

        /**
         * @param type the class to map
         * @return The resolved qualified name if one is defined.
         */
        public <T> T resolve(Class type, MediaType mt, Class<T> resolvedType) {
            T name = null;
            found : for (Resolver resolver : typeResolvers) {
                name = resolver.resolve(type, mt, resolvedType);
                if (name!=null) break found;
            }
            return name;
        }
    }

    /**
     * Perform any post create functions such as generating grammars.
     * @return A map of extra files to the content of those file encoded in UTF-8
     * @throws Exception
     */
    public ExternalGrammarDefinition createExternalGrammar();

    /**
     * Process the elements in the WADL definition to attach schema types
     * as required.
     * @param description The root description used to resolve these entries
     */
    public void attachTypes(ApplicationDescription description);

}

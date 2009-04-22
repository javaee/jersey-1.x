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

package com.sun.jersey.api.client.config;

import com.sun.jersey.api.client.Client;
import java.util.Map;
import java.util.Set;

/**
 * The client configuration that declares common property names,
 * features, properties, provider classes and singleton instances that
 * may be used by a {@link Client} instance.
 * <p>
 * An instance of this interface may be passed to the {@link Client} when
 * the client is created as follows:
 * <p>
 * <blockquote><pre>
 *     ClientConfig cc = ...
 *     Client c = Client.create(cc);
 * </pre></blockquote>
 * The client configuration may be used to register provider classes such
 * as those, for example, that support JAXB with JSON as follows:
 * <blockquote><pre>
 *     ClientConfig cc = new DefaultClientConfig();
 *     cc.getClasses().add(com.sun.jersey.impl.provider.entity.JSONRootElementProvider.class);
 *     Client c = Client.create(cc);
 * </pre></blockquote>
 * Alternatively an implementaton of ClientConfig could perform such
 * registeration.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface ClientConfig {
    /**
     * Redirection property. A value of "true" declares that the client will 
     * automatically redirect to the URI declared in 3xx responses.
     * 
     * The value MUST be an instance of {@link java.lang.Boolean}.
     * If the property is absent then the default value is "true".
     */
    public static final String PROPERTY_FOLLOW_REDIRECTS = 
            "com.sun.jersey.client.property.followRedirects";
    
    /**
     * Read timeout interval property, in milliseconds.
     * 
     * The value MUST be an instance of {@link java.lang.Integer}.
     * 
     * If the property is absent then the default value is an interval of
     * infinity. A value of zero 0 is equivalent to an interval of
     * infinity
     */
    public static final String PROPERTY_READ_TIMEOUT = 
            "com.sun.jersey.client.property.readTimeout";
    
    /**
     * Connect timeout interval property, in milliseconds.
     * 
     * The value MUST be an instance of {@link java.lang.Integer}.
     * 
     * If the property is absent then the default value is an interval of
     * infinity. A value of  0 is equivalent to an interval of
     * infinity
     */
    public static final String PROPERTY_CONNECT_TIMEOUT = 
            "com.sun.jersey.client.property.connectTimeout";
    
    /**
     * Chunked encoding property.
     * 
     * The value MUST be an instance of {@link java.lang.Integer}.
     * 
     * If the property is absent then chunked encoding will not be used.
     * A value &lt = 0 declares that chunked encoding will be used with 
     * the default chunk size. A value &gt 0 declares that chunked encoding
     * will be used with the value as the declared chunk size.
     */
    public static final String PROPERTY_CHUNKED_ENCODING_SIZE = 
            "com.sun.jersey.client.property.chunkedEncodingSize";

    /**
     * A value of "true" declares that the client will
     * automatically buffer the response entity (if any) and close resources
     * when a UniformInterfaceException is thrown.
     *
     * The value MUST be an instance of {@link java.lang.Boolean}.
     * If the property is absent then the default value is "true".
     */
    public static final String PROPERTY_BUFFER_RESPONSE_ENTITY_ON_EXCEPTION =
            "com.sun.jersey.client.property.bufferResponseEntityOnException";

    /**
     * Get the set of provider classes to be instantiated in the scope
     * of the Client
     * <p>
     * A provider class is a Java class with a {@link javax.ws.rs.ext.Provider} 
     * annotation declared on the class that implements a specific service 
     * interface.
     * 
     * @return the mutable set of provider classes. After intialization of
     *         the client modification of this value will have no effect.
     *         The returned value shall never be null.
     */
    Set<Class<?>> getClasses();
    
    /**
     * Get the singleton provider instances to be utilized by the client.
     * <p>
     * When the client is initialized the set of provider instances
     * will be combined and take precendence over the instances of provider 
     * classes. 
     * 
     * @return the mutable set of provider instances. After intialization of
     *         the client modification of this value will have no effect.
     *         The returned value shall never be null.
     */
    public Set<Object> getSingletons();
    
    /**
     * Get the map of features associated with the client.
     *
     * @return the features.
     *         The returned value shall never be null.
     */
    Map<String, Boolean> getFeatures();
    
    /**
     * Get the value of a feature.
     *
     * @param featureName the feature name.
     * @return true if the feature is present and set to true, otherwise false
     *         if the feature is present and set to false or the feature is not 
     *         present.
     */
    boolean getFeature(String featureName);
    
    /**
     * Get the map of properties associated with the client.
     *
     * @return the properties.
     *         The returned value shall never be null.
     */
    Map<String, Object> getProperties();

    /**
     * Get the value of a property.
     *
     * @param propertyName the property name.
     * @return the property, or null if there is no property present for the
     *         given property name.
     */
    Object getProperty(String propertyName);
    
    /**
     * Get a feature that is boolean property of the property bag.
     *
     * @param featureName the name of the feature;
     * @return true if the feature value is present and is an instance of
     *         <code>Boolean</code> and that value is true, otherwise false.
     */
    public boolean getPropertyAsFeature(String featureName);
}

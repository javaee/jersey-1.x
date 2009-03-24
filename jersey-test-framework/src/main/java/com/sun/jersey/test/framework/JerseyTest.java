/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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


package com.sun.jersey.test.framework;

import com.sun.jersey.test.framework.util.ApplicationDescriptor;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.test.framework.impl.JerseyAppContainer;
import com.sun.jersey.test.framework.impl.util.CommonUtils;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;

/**
 * An abstract test class which has all the calls for creating, starting and stopping
 * a test container. This class might be extended by user test classes which need to
 * just implement their test methods annotated by the JUnit 4.x @Test annotation. Also,
 * the user test class is expected to pass the contextName, servletPath (optional) and
 * resourcePackage info to the constructor of this class.
 * <p>
 * The container to test with, is set using the parameter <i>container.type</i>.
 * This parameter can take one of the following types:
 * <ol>
 *  <li>GrizzlyWeb</li>
 *  <li>HTTPServer</li>
 *  <li>EmbeddedGF</li>
 * </ol>
 * The default is <i>GrizzlyWeb</i> which runs the tests on Grizzly Web Container.
 * </p>
 * <p>
 *    If the user wants to enable client side logging, that can be done by just
 * setting the parameter <i>enableLogging</i>.
 * </p>
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public abstract class JerseyTest implements TestConstants {

    /**
     * Container type to be used for deployments.
     */
    protected final String CONTAINER_TYPE;

    /**
     * Init params for the servlet.
     */
    protected Map<String, String> INIT_PARAMS;

    protected Map<String, String> CONTEXT_PARAMS;

    /**
     * The base uri for the resources.
     */
    protected URI BASE_URI;

    /**
     * Holds the resource context-name.
     */
    protected String contextPath;

    /**
     * Holds the servlet url-pattern.
     */
    protected String servletPath;

    private Class servletClass;

    private String contextListenerClassName;

    /**
     * Holds the root resource package name.
     */
    protected String resourcePackage;

    /**
     * Handle to the container on which deployments are done.
     */
    protected JerseyAppContainer container;

    /**
     * The no-argument constructor. It just initialzes some members.
     * The user test needs to create an ApplicationDescriptor instance, to which all
     * the application related information like rootResourcePackageName, contextPath, etc. is set.
     * The user test has to explicitly call the setupTestEnvironment() method, which would
     * take care of starting the container and deploying the application.
     * @throws java.lang.Exception
     */
    public JerseyTest() throws Exception {
        CONTAINER_TYPE = getContainerType();
        INIT_PARAMS = new HashMap<String, String>();
        CONTEXT_PARAMS = new HashMap<String, String>();
    }

    /**
     * The constructor to be called when the application doesn't have any context path,
     * servletPath, contextParams, etc. The call for starting the container is made inside the constructor,
     * the setupTestContainer() method should not be called by the user explicitly in this case.
     * @param resourcePackageName
     * @throws java.lang.Exception
     */
    public JerseyTest(String resourcePackageName) throws Exception {
        CONTAINER_TYPE = getContainerType();
        INIT_PARAMS = new HashMap<String, String>();
        CONTEXT_PARAMS = new HashMap<String, String>();
        ApplicationDescriptor appDescriptor = new ApplicationDescriptor()
                .setRootResourcePackageName(resourcePackageName);
        setupTestEnvironment(appDescriptor);
    }

    /**
     * The constructor to be called when the test just needs to pass contextPath, servletPath and
     * root resource packages. The call for starting the container is made inside the constructor,
     * the setupTestContainer() should not be called by the user explicitly in this case.
     * @param contextPath
     * @param servletPath
     * @param resourcePackageName
     * @throws java.lang.Exception
     */
    public JerseyTest(String contextPath, String servletPath, String resourcePackageName) throws Exception {
        CONTAINER_TYPE = getContainerType();
        INIT_PARAMS = new HashMap<String, String>();
        CONTEXT_PARAMS = new HashMap<String, String>();
        ApplicationDescriptor appDescriptor = new ApplicationDescriptor()
                .setRootResourcePackageName(resourcePackageName)
                .setContextPath(contextPath)
                .setServletPath(servletPath);
        setupTestEnvironment(appDescriptor);
    }

    /**
     * Handle to the resources.
     */
    protected WebResource webResource;

    /**
     * Handle to the Jersey client.
     */
    protected Client jerseyClient;


    /**
     * Initial setup for the tests.
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        boolean setLogging = (System.getProperty("enableLogging") != null)
                ? true : false;
        jerseyClient = Client.create();
        if(setLogging) {
            jerseyClient.addFilter(new LoggingFilter());
        }
        webResource = jerseyClient.resource(BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        container.stopServer();
    }
   
    /**
     * Get the type of container to be used for deployments.
     * @return
     */
    protected String getContainerType() {
        String containerType = System.getProperty("container.type");
        return ((containerType != null) ? containerType : GRIZZLY_WEB_CONTAINER);
    }

    /**
     * Set the context name.
     * @param contextPath
     */
    protected void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Set the url-pattern for the servlet.
     * @param servletPath
     */
    protected void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    /**
     * Set the root resource package.
     * @param resourcePackage
     */
    protected void setResourcePackage(String resourcePackage) {
        this.resourcePackage = resourcePackage;
    }

    /**
     * A call to the method takes care of setting all the test related properties,
     * like the root resource package name, the application context path, servlet
     * url pattern, any init params, etc., and initialising and starting the
     * test run container.
     * @param appDescriptor
     * @throws java.lang.Exception
     */
    public void setupTestEnvironment(ApplicationDescriptor appDescriptor) throws Exception {
        setContextPath(appDescriptor.getContextPath());
        setServletPath(appDescriptor.getServletPath());
        setResourcePackage(appDescriptor.getRootResourcePackageName());
        BASE_URI = CommonUtils.getBaseURI(contextPath, servletPath);
        INIT_PARAMS.put(PackagesResourceConfig.PROPERTY_PACKAGES,
                resourcePackage);
        if(appDescriptor.getContextParams() != null) {
            CONTEXT_PARAMS.putAll(appDescriptor.getContextParams());
        }
        servletClass = appDescriptor.getServletClass();
        contextListenerClassName = appDescriptor.getContextListenerClassName();
        container = new JerseyAppContainer(CONTAINER_TYPE, appDescriptor);
        container.startServer();
    }
    
}
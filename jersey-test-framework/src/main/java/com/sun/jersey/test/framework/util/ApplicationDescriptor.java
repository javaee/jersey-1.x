/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.test.framework.util;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import java.util.Map;

/**
 *
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class ApplicationDescriptor {

    /**
     * The servlet path or the url-pattern mapping the servlet.
     */
    private String servletPath;

    /**
     * The app context name.
     */
    private String contextPath;

    /**
     * The servlet init params.
     */
    private Map<String, String> servletInitParams;

    /**
     * The context params.
     */
    private Map<String, String> contextParams;

    /**
     * The servlet class.
     */
    private Class servletClass = ServletContainer.class;

    /**
     * The context listener class name.
     */
    private String contextListenerClassName;

    /**
     * The root resource package name.
     */
    private String rootResourcePackageName;

    public String getContextListenerClassName() {
        return contextListenerClassName;
    }

    public ApplicationDescriptor setContextListenerClassName(String contextListenerClassName) {
        this.contextListenerClassName = contextListenerClassName;
        return this;
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }

    public ApplicationDescriptor setContextParams(Map<String, String> contextParams) {
        this.contextParams = contextParams;
        return this;
    }

    public String getContextPath() {
        return contextPath;
    }

    public ApplicationDescriptor setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }

    public String getRootResourcePackageName() {
        return rootResourcePackageName;
    }

    public ApplicationDescriptor setRootResourcePackageName(String rootResourcePackageName) {
        this.rootResourcePackageName = rootResourcePackageName;
        return this;
    }

    public Class getServletClass() {
        return servletClass;
    }

    public ApplicationDescriptor setServletClass(Class servletClass) {
        this.servletClass = servletClass;
        return this;
    }

    public Map<String, String> getServletInitParams() {
        return servletInitParams;
    }

    public ApplicationDescriptor setServletInitParams(Map<String, String> servletInitParams) {
        this.servletInitParams = servletInitParams;
        return this;
    }

    public String getServletPath() {
        return servletPath;
    }

    public ApplicationDescriptor setServletPath(String servletPath) {
        this.servletPath = servletPath;
        return this;
    }

}
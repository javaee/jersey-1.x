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

package com.sun.jersey.test.framework.web.jaxb.types;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Schema representation of the web.xml entries.
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType (name="web-app", propOrder={
    "contextParam",
    "listeners",
    "servletType",
    "servletMapping"
})
@XmlRootElement(name = "web-app")
public class WebAppType {

    @XmlElement(name="context-param")
    private List<ContextParamType> contextParam;

    @XmlElement(name="listener")
    private List<ListenerType> listeners;

    @XmlElement(name="servlet", required=true)
    private ServletType servletType;

    @XmlElement(name="servlet-mapping", required=true)
    private ServletMappingType servletMapping;

    public List<ContextParamType> getContextParam() {
        return contextParam;
    }

    public void setContextParam(List<ContextParamType> contextParam) {
        this.contextParam = contextParam;
    }

    public List<ListenerType> getListeners() {
        return listeners;
    }

    public void setListeners(List<ListenerType> listeners) {
        this.listeners = listeners;
    }

    public ServletMappingType getServletMapping() {
        return servletMapping;
    }

    public void setServletMapping(ServletMappingType servletMapping) {
        this.servletMapping = servletMapping;
    }

    public ServletType getServletType() {
        return servletType;
    }

    public void setServletType(ServletType servletType) {
        this.servletType = servletType;
    }

}
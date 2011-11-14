/*
 *
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
 * /
 */
package com.sun.jersey.moxy;

import com.sun.jersey.core.util.FeaturesAndProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.eclipse.persistence.jaxb.JAXBContextFactory;

/**
 *
 * @author Jakub Podlesak
 */
@Provider
public class MoxyContextResolver implements ContextResolver<JAXBContext> {

    public static final String PROPERTY_MOXY_OXM_PACKAGE_NAMES = "com.sun.jersey.moxy.config.property.packages";
    public static final String PROPERTY_MOXY_OXM_MAPPING_URL = "com.sun.jersey.moxy.config.property.oxm.mapping.url";

    final Set<String> oxmPackageNames = new HashSet<String>();
    final String oxmMappingUrl;

    public MoxyContextResolver(@Context FeaturesAndProperties fap) {
        oxmPackageNames.addAll(getPackageNames(fap.getProperty(PROPERTY_MOXY_OXM_PACKAGE_NAMES)));
        oxmMappingUrl = getStringValue(fap.getProperty(PROPERTY_MOXY_OXM_MAPPING_URL));
    }

    private String getStringValue(Object u) {
        if (u == null) {
            return null;
        } else if (u instanceof String) {
            return (String)u;
        } else {
            throw new IllegalArgumentException(PROPERTY_MOXY_OXM_MAPPING_URL + " must " +
                    "have a property value of type String");
        }
    }

    private List<String> getPackageNames(Object p) {
        if (p == null) {
            return Collections.EMPTY_LIST;
        } else if (p instanceof String) {
            return Arrays.asList(Helper.getElements(new String[]{(String)p}));
        } else if (p instanceof String[]) {
            return Arrays.asList(Helper.getElements((String[])p));
        } else {
            throw new IllegalArgumentException(PROPERTY_MOXY_OXM_PACKAGE_NAMES + " must " +
                    "have a property value of type String or String[]");
        }
    }

    @Override
    public JAXBContext getContext(Class<?> type) {

        final String typePackageName = type.getPackage().getName();

        if (!oxmPackageNames.contains(typePackageName)) {
            return null;
        }

        Map<String, Source> metadata = new HashMap<String, Source>();
        metadata.put(typePackageName,
                (oxmMappingUrl != null) ? new StreamSource(oxmMappingUrl)
                                            : new StreamSource(type.getResourceAsStream("eclipselink-oxm.xml")));

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(JAXBContextFactory.ECLIPSELINK_OXM_XML_KEY, metadata);

        try {
            return JAXBContext.newInstance(typePackageName, Thread.currentThread().getContextClassLoader(), properties);
        } catch (JAXBException ex) {
            throw new WebApplicationException(ex);
        }
    }
}

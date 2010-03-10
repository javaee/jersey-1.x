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

package com.sun.jersey.server.linking;

import com.sun.jersey.api.uri.UriTemplateParser;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Utility class that can inject links into {@link Link} annotated fields in
 * an entity.
 * @author mh124079
 */
public class LinkProcessor<T> {

    // Maintains an internal static cache to optimize processing

    private static Map<Class<?>, LinkProcessor> processors
            = new HashMap<Class<?>, LinkProcessor>();

    /**
     * Get a LinkProcessor for the supplied class. An internal cache is
     * maintained to prevent repeated inpection of the same class.
     * @param c
     * @return
     */
    public static synchronized <T> LinkProcessor<T> getInstance(Class<T> c) {
        if (processors.containsKey(c)) {
            return processors.get(c);
        } else {
            LinkProcessor processor = new LinkProcessor(c);
            processors.put(c, processor);
            return processor;
        }
    }

    // Members

    private EntityDescriptor entityDescriptor;

    private LinkProcessor(Class<T> c) {
        entityDescriptor = new EntityDescriptor(c);
    }

    /**
     * Inject any {@link Link} annotated fields in the supplied entity.
     * @param entity
     * @param uriInfo
     */
    public void processLinks(T entity, UriInfo uriInfo) {
        Set<Object> processed = new HashSet<Object>();
        processLinks(entity, processed, uriInfo);
    }

    /**
     * Inject any {@link Link} annotated fields in the supplied entity.
     * @param entity
     * @param processed a list of already processed objects, used to break
     * recursion when processing circular references.
     * @param uriInfo
     */
    private void processLinks(Object entity, Set<Object> processed,
            UriInfo uriInfo) {
        if (entity==null || processed.contains(entity))
            return; // ignore null properties and defeat circular references
        processed.add(entity);

        // Process any @Link annotated fields in entity
        for (LinkFieldDescriptor d: entityDescriptor.getLinkFields()) {
            String template = d.getLinkTemplate();
            UriBuilder ub=applyLinkStyle(template, d.getLinkStyle(), uriInfo);
            UriTemplateParser parser = new UriTemplateParser(template);
            List<String> parameterNames = parser.getNames();
            URI uri = ub.buildFromMap(entityDescriptor.getValueMap(parameterNames, entity));
            d.setPropertyValue(entity, uri);
        }

        // If entity is an array or collection then process members
        Class<?> entityClass = entity.getClass();
        if (entityClass.isArray() && Object[].class.isAssignableFrom(entityClass)) {
            Object array[] = (Object[])entity;
            for (Object member: array) {
                processMember(member, processed, uriInfo);
            }
        } else if (entity instanceof Collection) {
            Collection collection = (Collection)entity;
            for (Object member: collection) {
                processMember(member, processed, uriInfo);
            }
        }

        // Recursively process all member fields
        for (FieldDescriptor member: entityDescriptor.getNonLinkFields()) {
            processMember(member.getFieldValue(entity), processed, uriInfo);
        }
    }

    private void processMember(Object member, Set<Object> processed, UriInfo uriInfo) {
        if (member != null) {
            LinkProcessor proc = LinkProcessor.getInstance(member.getClass());
            proc.processLinks(member, processed, uriInfo);
        }
    }

    private UriBuilder applyLinkStyle(String template, Link.Style style, UriInfo uriInfo) {
        UriBuilder ub=null;
        switch (style) {
            case ABSOLUTE:
                ub = uriInfo.getBaseUriBuilder().path(template);
                break;
            case ABSOLUTE_PATH:
                String basePath = uriInfo.getBaseUri().getPath();
                ub = UriBuilder.fromPath(basePath).path(template);
                break;
            case RELATIVE_PATH:
                ub = UriBuilder.fromPath(template);
                break;
        }
        return ub;
    }

}

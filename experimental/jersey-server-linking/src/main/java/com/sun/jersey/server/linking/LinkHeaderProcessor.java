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

import com.sun.jersey.server.linking.el.LinkBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

/**
 * Processes @LinkHeader and @LinkHeaders annotations on entity classes and
 * adds appropriate HTTP Link headers.
 * @author mh124079
 */
public class LinkHeaderProcessor<T> {
    private EntityDescriptor instanceDescriptor;

    public LinkHeaderProcessor(Class<T> c) {
        instanceDescriptor = EntityDescriptor.getInstance(c);
    }

    /**
     * Process any {@link LinkHeader} annotations on the supplied entity.
     * @param entity the entity object returned by the resource method
     * @param uriInfo the uriInfo for the request
     * @param headers the map into which the headers will be added
     */
    public void processLinkHeaders(T entity, UriInfo uriInfo, MultivaluedMap<String, Object> headers) {
        List<String> headerValues = getLinkHeaderValues(entity, uriInfo);
        for (String headerValue: headerValues) {
            headers.add("Link", headerValue);
        }
    }

    List<String> getLinkHeaderValues(Object entity, UriInfo uriInfo) {
        Object resource = uriInfo.getMatchedResources().get(0);
        List<String> headerValues = new ArrayList<String>();
        for (LinkHeaderDescriptor desc: instanceDescriptor.getLinkHeaders()) {
            String headerValue = getLinkHeaderValue(desc, entity, resource, uriInfo);
            headerValues.add(headerValue);
        }
        return headerValues;
    }

    static String getLinkHeaderValue(LinkHeaderDescriptor desc, Object entity, Object resource, UriInfo uriInfo) {
        URI uri = LinkBuilder.buildURI(desc, entity, resource, entity, uriInfo);
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(uri.toString());
        builder.append(">");
        LinkHeader header = desc.getLinkHeader();
        appendOptionalParameter(builder, "rel", header.rel(), true);
        appendOptionalParameter(builder, "rev", header.rev(), true);
        appendOptionalParameter(builder, "type", header.type(), true);
        appendOptionalParameter(builder, "title", header.title(), true);
        appendOptionalParameter(builder, "anchor", header.anchor(), true);
        appendOptionalParameter(builder, "media", header.media(), true);
        appendOptionalParameter(builder, "hreflang", header.hreflang(), false);
        for (LinkHeader.Extension ext: header.extensions()) {
            appendOptionalParameter(builder, ext.name(), ext.value(), ext.quoteValue());
        }
        return builder.toString();
    }

    private static void appendOptionalParameter(StringBuilder builder, String name, String value, boolean quoteValue) {
        if (value!=null && value.length()>0) {
            builder.append(";");
            builder.append(name);
            builder.append("=");
            if (quoteValue)
                builder.append("\"");
            builder.append(value);
            if (quoteValue)
                builder.append("\"");
        }
    }

}

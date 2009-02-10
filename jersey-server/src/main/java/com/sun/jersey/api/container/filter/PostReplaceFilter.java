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
package com.sun.jersey.api.container.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * A filter to support HTTP method replacing of a POST request to a request 
 * utilizing another HTTP method for the case where proxies or HTTP
 * servers would otherwise block that HTTP method.
 * <p>
 * This filter may be used to replace a POST request with a PUT or DELETE
 * request.
 * <p>
 * Replacement will occur if the request method is POST and there exists an
 * a request header "X-HTTP-Method-Override" with a non-empty value. That value
 * will be the HTTP method that replaces the POST method.
 * <p>
 * When an application is deployed as a Servlet or Filter this Jersey filter can be
 * registered using the following initialization parameter:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerRequestFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.PostReplaceFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt
 * </pre></blockquote>
 * <p>
 * TODO support query parameter for declaring the replacing method.
 * 
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.container.filter
 */
public class PostReplaceFilter implements ContainerRequestFilter {

    public ContainerRequest filter(ContainerRequest request) {
        if (!request.getMethod().equalsIgnoreCase("POST"))
            return request;
        
        String override = request.getRequestHeaders().getFirst("X-HTTP-Method-Override");
        if (override == null)
            return request;        
        override = override.trim();
        if (override.length() == 0)
            return request;
        
        request.setMethod(override);
        return request;
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2013 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.samples.freemarker.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.view.Viewable;

/**
 * Sample resource to demonstrate how to work with explicit
 * and implicit FreeMarker templates.
 *
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
public class FreemarkerResource {

    /**
     * This is to populate the actual MVC model (FreemarkerResource instance)
     * with a request related content.
     */
    @QueryParam("user")
    @DefaultValue("Anonymous")
    String user;

    /**
     * Actual resource instance acts as the implicit view model.
     * User field is required by the implicit, index.ftl, template.
     *
     * @return actual user
     */
    public String getUser() {
        return user;
    }

    /**
     * We need to create a {@link Viewable} instance manually here.
     *
     * @return Viewable mapped to the hello.ftl template.
     */
    @GET
    @Path("explicit")
    public Viewable getHello() {
        // "/hello.ftl" would work the same way bellow
        return new Viewable("/hello", createModel());
    }

    private Map<String, Object> createModel() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user", "Bee Gees");
        List<String> l = new ArrayList<String>();
        l.add("to remember me");
        l.add("and the love that used to be");
        l.add("...");
        map.put("items", l);
        return map;
    }
}

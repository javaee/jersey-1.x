/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.atom.abdera.impl.provider.entity;

import com.sun.jersey.atom.abdera.ContentHelper;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import org.apache.abdera.model.Entry;

/**
 * <p>Server side resource class for the unit test suite.</p>
 */
@Path("/test")
public class TestingResource {

    @Context
    ContentHelper contentHelper;

    @Context
    Request request;

    @GET
    @Path("categories")
    @Produces({"application/atomcat+xml", "application/xml", "text/xml", "application/atomcat+json", "application/json"})
    public Response getCategories() {
        dumpSelectedMediaType("getCategories");
        return Response.ok(TestingFactory.createCategories()).build();
    }

    @GET
    @Path("content")
    @Produces("application/xml")
    public Response getContent() {
        dumpSelectedMediaType("getContent");
        Entry entry = TestingFactory.createEntry();
        ContentBean bean = new ContentBean("foo value", "bar value");
        contentHelper.setContentEntity(entry, MediaType.APPLICATION_XML_TYPE, bean);
        return Response.ok(entry).build();
    }

    @GET
    @Path("entry")
    @Produces({"application/atom", "application/atom+xml", "application/xml", "text/xml", "application/atom+json", "application/json"})
    public Response getEntry() {
        dumpSelectedMediaType("getEntry");
        return Response.ok(TestingFactory.createEntry()).build();
    }

    @GET
    @Path("feed")
    @Produces({"application/atom", "application/atom+xml", "application/xml", "text/xml", "application/atom+json", "application/json"})
    public Response getFeed() {
        dumpSelectedMediaType("getFeed");
        return Response.ok(TestingFactory.createFeed()).build();
    }

    @GET
    @Path("service")
    @Produces({"application/atomsvc+xml", "application/xml", "text/xml", "application/atomsvc+json", "application/json"})
    public Response getService() {
        dumpSelectedMediaType("getService");
        return Response.ok(TestingFactory.createService()).build();
    }

    private void dumpSelectedMediaType(String methodName) {
        System.out.println("Method " + methodName + " selected media type " + calculateSelectedMediaType(methodName));
    }

    /**
     * <p>Calculate and return the media type that we should be producing
     * on this call, following the same algorithm that JAX-RS does.</p>
     *
     * @param methodName Name of the method (in this class) that was called
     *
     * @exception IllegalArgumentException if we cannot identify the appropriate
     *  @Produces annotation for this method name
     */
    private MediaType calculateSelectedMediaType(String methodName) {
        // Reflect to get the Method definition for this methodName
        // The following logic assumes none of the method names are overloaded,
        // which is true for this class but might not be in general
        Method[] methods = this.getClass().getDeclaredMethods();
        Method method = null;
        for (int i = 0; i < methods.length; i++) {
            if (methodName.equals(methods[i].getName())) {
                method = methods[i];
                break;
            }
        }
        if (method == null) {
            throw new IllegalArgumentException("Cannot find Method instance for method " + methodName);
        }
        // Look for the @Produces annotation on this method
        Annotation annotation = method.getAnnotation(Produces.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Method " + methodName + " does not have an @Produces annotation");
        }
        // Build a list of Variants based on the @Produces annotation
        List<Variant> variants = new ArrayList<Variant>();
        String[] items = ((Produces) annotation).value();
        for (String item : items) {
            String[] parts = item.split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Media type " + item + " on @Produces list for method " + methodName + " is malformed");
            }
            variants.add(new Variant(new MediaType(parts[0], parts[1]), null, null));
        }
        // Return the media type from the variant that was selected
        return request.selectVariant(variants).getMediaType();
    }

}

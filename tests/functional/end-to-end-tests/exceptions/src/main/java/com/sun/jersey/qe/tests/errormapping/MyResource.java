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

package com.sun.jersey.test.functional.errormapping;

import com.sun.jersey.api.container.MappableContainerException;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Path("/myresource")
public class MyResource {
    
    @GET 
    @Produces("text/plain")
    public String getIt() {
        return "Hi there!";
    }

    @Path("runtime")
    @GET
    @Produces("text/plain")
    public String runtimeException(@QueryParam("ex") String className) 
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class c = Class.forName(className, true, this.getClass().getClassLoader());
        RuntimeException re = (RuntimeException)c.newInstance();
        if (true) throw re;
        return "Hi there!";
    }

    @Path("checked/ioexception")
    @GET
    @Produces("text/plain")
    public String checkedIOException() throws IOException {
        if (true) throw new IOException();
        return "Hi there!";
    }

    public static class MyException extends Exception {
    }

    @Path("checked/myexception")
    @GET
    @Produces("text/plain")
    public String checkedMyException() throws MyException {
        if (true) throw new MyException();
        return "Hi there!";
    }


    public static class MyMappedException extends Exception {
    }

    @Provider
    public static class MyMappedExceptionMapper implements ExceptionMapper<MyMappedException> {
        public Response toResponse(MyMappedException exception) {
            return Response.serverError().entity("Jersey mapped exception: " + exception.getClass().getName()).build();
        }
    }

    @Path("checked/mymappedexception")
    @GET
    @Produces("text/plain")
    public String checkedMyMappedException() throws MyMappedException {
        if (true) throw new MyMappedException();
        return "Hi there!";
    }


    public static class MyMappedRuntimeException extends RuntimeException {
    }

    @Provider
    public static class MyMappedRuntimeExceptionMapper implements ExceptionMapper<MyMappedRuntimeException> {
        public Response toResponse(MyMappedRuntimeException exception) {
            return Response.serverError().entity("Jersey mapped runtime exception: " + exception.getClass().getName()).build();
        }
    }

    @Path("checked/mymappedruntimeexception")
    @GET
    @Produces("text/plain")
    public String checkedMyMappedRuntimeException() {
        if (true) throw new MyMappedRuntimeException();
        return "Hi there!";
    }



    public static class MyMappedThrowingException extends Exception {
    }

    @Provider
    public static class MyMappedThrowingExceptionMapper implements ExceptionMapper<MyMappedThrowingException> {
        public Response toResponse(MyMappedThrowingException exception) {
            throw new MappableContainerException(new IOException());
        }
    }

    @Path("checked/mymappedthrowingexception")
    @GET
    @Produces("text/plain")
    public String checkedMyMappedThrowingException() throws MyMappedThrowingException {
        if (true) throw new MyMappedThrowingException();
        return "Hi there!";
    }

    @Path("webapplicationexception/{status}")
    @GET
    @Produces("text/plain")
    public String webApplicationException(
            @PathParam("status") int status,
            @QueryParam("r") String r) {
        if (true) {
            Response resp = (r == null)
                ? Response.status(status).build()
                : Response.status(status).entity(r).build();

            throw new WebApplicationException(resp);
        }
        return "Hi there!";
    }
}

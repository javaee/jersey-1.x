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

package com.sun.jersey.impl.methodparams;

import com.sun.jersey.api.ParamException;
import com.sun.jersey.api.ParamException.CookieParamException;
import com.sun.jersey.api.ParamException.FormParamException;
import com.sun.jersey.api.ParamException.HeaderParamException;
import com.sun.jersey.api.ParamException.MatrixParamException;
import com.sun.jersey.api.ParamException.PathParamException;
import com.sun.jersey.api.ParamException.QueryParamException;
import com.sun.jersey.api.ParamException.URIParamException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.spi.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Path;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.StringReaderProvider;
import java.net.URI;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class StringReaderTest extends AbstractResourceTester {

    public StringReaderTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class BadDateResource {
        @GET
        public String doGet(@QueryParam("d") Date d) {
            return "DATE";
        }
    }
    
    public void testBadDateResource() {
        initiateWebApplication(BadDateResource.class);
        ClientResponse cr = resource("/", false).queryParam("d", "123").
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
    }


    @Path("/")
    public static class BadEnumResource {
        public enum ABC {
            A, B, C;
        };

        @GET
        public String doGet(@QueryParam("d") ABC d) {
            return "ENUM";
        }
    }

    public void testBadEnumResource() {
        initiateWebApplication(BadEnumResource.class);
        ClientResponse cr = resource("/", false).queryParam("d", "123").
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
    }


    public static class URIStringReaderProvider implements StringReaderProvider<URI> {

        public StringReader<URI> getStringReader(Class<?> type, Type genericType, Annotation[] annotations) {
            if (type != URI.class) return null;

            return new StringReader<URI>() {
                public URI fromString(String value) {
                    return URI.create(value);
                }
            };
        }

    }

    @Path("/")
    public static class BadURIResource {
        @GET
        public String doGet(@QueryParam("d") URI d) {
            return "URI";
        }
    }

    public void testBadURIResource() {
        initiateWebApplication(BadURIResource.class, URIStringReaderProvider.class);
        ClientResponse cr = resource("/", false).queryParam("d", " 123 ").
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
    }


    public static abstract class BaseExceptionMapper<T extends ParamException> implements ExceptionMapper<T> {
        public Response toResponse(T exception, String entity) {
            assertEquals("x", exception.getParameterName());
            if (exception.getParameterType() != PathParam.class) {
                assertEquals("default", exception.getDefaultStringValue());
            }
            return Response.fromResponse(exception.getResponse()).entity(entity).build();
        }
    }

    public static class ParamExceptionMapper extends BaseExceptionMapper<ParamException> {
        public Response toResponse(ParamException exception) {
            return toResponse(exception, "param");
        }
    }

    public static class URIExceptionMapper extends BaseExceptionMapper<URIParamException> {
        public Response toResponse(URIParamException exception) {
            return toResponse(exception, "uri");
        }
    }

    public static class PathExceptionMapper extends BaseExceptionMapper<PathParamException> {
        public Response toResponse(PathParamException exception) {
            return toResponse(exception, "path");
        }
    }

    public static class MatrixExceptionMapper extends BaseExceptionMapper<MatrixParamException> {
        public Response toResponse(MatrixParamException exception) {
            return toResponse(exception, "matrix");
        }
    }

    public static class QueryExceptionMapper extends BaseExceptionMapper<QueryParamException> {
        public Response toResponse(QueryParamException exception) {
            return toResponse(exception, "query");
        }
    }

    public static class CookieExceptionMapper extends BaseExceptionMapper<CookieParamException> {
        public Response toResponse(CookieParamException exception) {
            return toResponse(exception, "cookie");
        }
    }

    public static class HeaderExceptionMapper extends BaseExceptionMapper<HeaderParamException> {
        public Response toResponse(HeaderParamException exception) {
            return toResponse(exception, "header");
        }
    }

    public static class FormExceptionMapper extends BaseExceptionMapper<FormParamException> {
        public Response toResponse(FormParamException exception) {
            return toResponse(exception, "form");
        }
    }

    @Path("/")
    public static class ParamExceptionMapperResource {
        @Path("path/{x}")
        @GET
        public String getPath(@PathParam("x") URI x) {
            return "";
        }

        @Path("matrix")
        @GET
        public String getMatrix(@DefaultValue("default") @MatrixParam("x") URI x) {
            return "";
        }

        @Path("query")
        @GET
        public String getQuery(@DefaultValue("default") @QueryParam("x") URI x) {
            return "";
        }

        @Path("cookie")
        @GET
        public String getCookie(@DefaultValue("default") @CookieParam("x") URI x) {
            return "";
        }

        @Path("header")
        @GET
        public String getHeader(@DefaultValue("default") @HeaderParam("x") URI x) {
            return "";
        }

        @Path("form")
        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        public String postForm(@DefaultValue("default") @FormParam("x") URI x) {
            return "";
        }
    }

    public void testParamException() {
        initiateWebApplication(ParamExceptionMapperResource.class, 
                PathExceptionMapper.class,
                MatrixExceptionMapper.class,
                QueryExceptionMapper.class,
                CookieExceptionMapper.class,
                HeaderExceptionMapper.class,
                FormExceptionMapper.class);

        ClientResponse cr = resource("/", false).path("path/ 123").
                get(ClientResponse.class);
        assertEquals("path", cr.getEntity(String.class));

        cr = resource("/", false).path("matrix;x= 123").
                get(ClientResponse.class);
        assertEquals("matrix", cr.getEntity(String.class));

        cr = resource("/query", false).queryParam("x", " 123").
                get(ClientResponse.class);
        assertEquals("query", cr.getEntity(String.class));

        cr = resource("/cookie", false).cookie(new Cookie("x", " 123")).
                get(ClientResponse.class);
        assertEquals("cookie", cr.getEntity(String.class));

        cr = resource("/header", false).header("x", " 123").
                get(ClientResponse.class);
        assertEquals("header", cr.getEntity(String.class));

        Form f = new Form();
        f.add("x", " 123");
        cr = resource("/form", false).
                post(ClientResponse.class, f);
        assertEquals("form", cr.getEntity(String.class));
    }

    public void testGeneralParamException() {
        initiateWebApplication(ParamExceptionMapperResource.class,
                ParamExceptionMapper.class);

        ClientResponse cr = resource("/", false).path("path/ 123").
                get(ClientResponse.class);
        assertEquals("param", cr.getEntity(String.class));

        cr = resource("/", false).path("matrix;x= 123").
                get(ClientResponse.class);
        assertEquals("param", cr.getEntity(String.class));

        cr = resource("/query", false).queryParam("x", " 123").
                get(ClientResponse.class);
        assertEquals("param", cr.getEntity(String.class));

        cr = resource("/cookie", false).cookie(new Cookie("x", " 123")).
                get(ClientResponse.class);
        assertEquals("param", cr.getEntity(String.class));

        cr = resource("/header", false).header("x", " 123").
                get(ClientResponse.class);
        assertEquals("param", cr.getEntity(String.class));

        Form f = new Form();
        f.add("x", " 123");
        cr = resource("/form", false).
                post(ClientResponse.class, f);
        assertEquals("param", cr.getEntity(String.class));
    }

    public void testURIParamException() {
        initiateWebApplication(ParamExceptionMapperResource.class,
                URIExceptionMapper.class);

        ClientResponse cr = resource("/", false).path("path/ 123").
                get(ClientResponse.class);
        assertEquals("uri", cr.getEntity(String.class));

        cr = resource("/", false).path("matrix;x= 123").
                get(ClientResponse.class);
        assertEquals("uri", cr.getEntity(String.class));

        cr = resource("/query", false).queryParam("x", " 123").
                get(ClientResponse.class);
        assertEquals("uri", cr.getEntity(String.class));
    }

}

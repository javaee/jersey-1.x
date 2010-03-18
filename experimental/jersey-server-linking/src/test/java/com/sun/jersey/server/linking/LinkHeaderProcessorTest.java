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

import com.sun.jersey.server.linking.LinkHeader.Extension;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import junit.framework.TestCase;

/**
 *
 * @author mh124079
 */
public class LinkHeaderProcessorTest extends TestCase {

    UriInfo mockUriInfo;

    public LinkHeaderProcessorTest(String name) {
        super(name);
        mockUriInfo = new UriInfo() {

            private final static String baseURI = "http://example.com/application/resources";

            public String getPath() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getPath(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<PathSegment> getPathSegments() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<PathSegment> getPathSegments(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public URI getRequestUri() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public UriBuilder getRequestUriBuilder() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public URI getAbsolutePath() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public UriBuilder getAbsolutePathBuilder() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public URI getBaseUri() {
                return getBaseUriBuilder().build();
            }

            public UriBuilder getBaseUriBuilder() {
                return UriBuilder.fromUri(baseURI);
            }

            public MultivaluedMap<String, String> getPathParameters() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MultivaluedMap<String, String> getPathParameters(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MultivaluedMap<String, String> getQueryParameters() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<String> getMatchedURIs() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<String> getMatchedURIs(boolean decode) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<Object> getMatchedResources() {
                Object dummyResource = new Object(){};
                return Collections.singletonList(dummyResource);
            }

        };
    }

    @LinkHeader(@Link(value="A"))
    public static class EntityA {
    }

    public void testLiteral() {
        System.out.println("Literal");
        LinkHeaderProcessor<EntityA> instance = new LinkHeaderProcessor(EntityA.class);
        EntityA testClass = new EntityA();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/A>", headerValue);
    }

    @LinkHeader(@Link(value="${entity.id}"))
    public static class EntityB {
        public String getId() {
            return "B";
        }
    }

    public void testEL() {
        System.out.println("EL");
        LinkHeaderProcessor<EntityB> instance = new LinkHeaderProcessor(EntityB.class);
        EntityB testClass = new EntityB();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/B>", headerValue);
    }

    @LinkHeader(@Link(value="{id}"))
    public static class EntityC {
        public String getId() {
            return "C";
        }
    }

    public void testTemplateLiteral() {
        System.out.println("Template Literal");
        LinkHeaderProcessor<EntityC> instance = new LinkHeaderProcessor(EntityC.class);
        EntityC testClass = new EntityC();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/C>", headerValue);
    }

    @LinkHeaders({
        @LinkHeader(@Link(value="A")),
        @LinkHeader(@Link(value="B"))
    })
    public static class EntityD {
    }

    public void testMultiple() {
        System.out.println("Multiple Literal");
        LinkHeaderProcessor<EntityD> instance = new LinkHeaderProcessor(EntityD.class);
        EntityD testClass = new EntityD();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo);
        assertEquals(2, headerValues.size());
        // not sure if annotation order is supposed to be preserved but it seems
        // to work as expected
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/A>", headerValue);
        headerValue = headerValues.get(1);
        assertEquals("</application/resources/B>", headerValue);
    }

    @LinkHeader(
        value=@Link(value="E"),
        rel="relE",
        rev="revE",
        type="type/e",
        title="titleE",
        anchor="anchorE",
        media="mediaE",
        hreflang="en-E",
        extensions={
            @Extension(name="e1", value="v1"),
            @Extension(name="e2", value="v2", quoteValue=false)
        }
    )
    public static class EntityE {
    }

    public void testParameters() {
        System.out.println("Parameters");
        LinkHeaderProcessor<EntityE> instance = new LinkHeaderProcessor(EntityE.class);
        EntityE testClass = new EntityE();
        List<String> headerValues = instance.getLinkHeaderValues(testClass, mockUriInfo);
        assertEquals(1, headerValues.size());
        String headerValue = headerValues.get(0);
        assertEquals("</application/resources/E>;rel=\"relE\";rev=\"revE\";type=\"type/e\";title=\"titleE\";anchor=\"anchorE\";media=\"mediaE\";hreflang=en-E;e1=\"v1\";e2=v2", headerValue);
    }
}

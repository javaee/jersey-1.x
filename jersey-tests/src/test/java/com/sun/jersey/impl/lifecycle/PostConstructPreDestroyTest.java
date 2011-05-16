/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.impl.lifecycle;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 *
 * @author Martin Matula
 */
public class PostConstructPreDestroyTest extends AbstractResourceTester {
    public static StringBuilder stringBuilder;
            
    public PostConstructPreDestroyTest(String testName) {
        super(testName);
    }

    @Path("/a")
    public static class ResourceA {
        protected final StringBuilder sb = new StringBuilder();

        @PostConstruct
        private void init() {
            sb.append("A");
        }

        @PreDestroy
        private void cleanup() {
            sb.append("A");
        }

        @GET
        public String getText() {
            stringBuilder = sb;
            return sb.toString();
        }
    }
    
    @Path("/b")
    public static class ResourceB extends ResourceA {
        @PostConstruct
        private void init() {
            sb.append("B");
        }

        @PreDestroy
        public void cleanup() {
            sb.append("B");
        }
    }

    @Path("/c")
    public static class ResourceC extends ResourceB {
        @PostConstruct
        protected void init2() {
            sb.append("C");
        }

        @PreDestroy
        private void cleanup2() {
            sb.append("C");
        }
    }

    @Path("/d")
    public static class ResourceD extends ResourceC {
        @PostConstruct
        protected void init2() {
            sb.append("D");
        }

        @PreDestroy
        private void cleanup2() {
            sb.append("D");
        }
    }

    public void testPostConstructPreDestroy() {
        initiateWebApplication(ResourceA.class, ResourceB.class, ResourceC.class, ResourceD.class);
        WebResource r = resource("/");
        String s = r.path("d").get(String.class);
        assertEquals("BD", s);

        w.destroy();
        
        assertEquals("BDDB", stringBuilder.toString());
    }
}
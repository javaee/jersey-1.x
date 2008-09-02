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
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.sun.jersey.impl.modelapi.validation;

import com.sun.jersey.impl.modelapi.validation.BasicValidator;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class BasicValidatorTest extends TestCase {
    
    @Path("rootNoCtor")
    public static class TestRootResourceWithoutPublicConstructor {
        
        private TestRootResourceWithoutPublicConstructor() {};

        @GET
        public String getIt() {
            return "it";
        }
    }

    public void ignoredTestRootResourceWithoutPublicConstructor() throws Exception {
        System.out.println("---\nAn issue should be reported if a public ctor is missing at a root resource:");
        AbstractResource ar = IntrospectionModeller.createResource(TestRootResourceWithoutPublicConstructor.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }

    @Path("rootAmbigCtors")
    public static class TestRootResourceAmbigCtors {
        
        public TestRootResourceAmbigCtors(@QueryParam("s") String p) {};

        public TestRootResourceAmbigCtors(@QueryParam("s") int p) {};

        @GET
        public String getIt() {
            return "it";
        }
    }

    public void ignoredTestRootResourceAmbigConstructors() throws Exception {
        System.out.println("---\nAn issue should be reported if more public ctors exists with the same number of params at a root resource:");
        AbstractResource ar = IntrospectionModeller.createResource(TestRootResourceAmbigCtors.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }
    
    @Singleton
    @Path("rootSingleton/{p}")
    public static class TestCantInjectFieldsForSingleton {
        @MatrixParam("m") String matrixParam;
        @QueryParam("q") String queryParam;
        @PathParam("p") String pParam;
        @CookieParam("c") String cParam;
        @HeaderParam("h") String hParam;
        
        @GET
        public String getIt() {
            return "it";
        }
    }
    
    public void ignoredTestSingletonFieldsInjection() throws Exception {
        System.out.println("---\nAn issue should be reported if injection is required for a singleton life-cycle:");
        AbstractResource ar = IntrospectionModeller.createResource(TestCantInjectFieldsForSingleton.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        assertEquals(5, validator.getIssueList().size());
        printIssueList(validator);
    }

    public static class TestNonPublicRM {
        @GET
        private String getIt() {
            return "this";
        }
    }
    
    public void testNonPublicRM() throws Exception {
        System.out.println("---\nAn issue should be reported if a resource method is not public:");
        AbstractResource ar = IntrospectionModeller.createResource(TestNonPublicRM.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }
    
    public static class TestMoreThanOneEntity {
        @PUT
        public void put(String one, String two) {
        }
    }
    
    public void ignoredTestMoreThanOneEntity() throws Exception {
        System.out.println("---\nAn issue should be reported if a resource method takes more than one entity params:");
        AbstractResource ar = IntrospectionModeller.createResource(TestMoreThanOneEntity.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }
    
    
    public static class TestGetRMReturningVoid {
        @GET
        public void getMethod() {
        }
    }
    
    public void testGetRMReturningVoid() throws Exception {
        System.out.println("---\nAn issue should be reported if a get method returns void:");
        AbstractResource ar = IntrospectionModeller.createResource(TestGetRMReturningVoid.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }
    
    public static class TestSRLReturningVoid {
        @Path("srl")
        public void srLocator() {
        }
    }
    
    public void testSRLReturningVoid() throws Exception {
        System.out.println("---\nAn issue should be reported if a sub-resource locator returns void:");
        AbstractResource ar = IntrospectionModeller.createResource(TestSRLReturningVoid.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }
   
    public static class TestGetSRMReturningVoid {
        @GET @Path("srm")
        public void getSRMethod() {
        }
    }
    
    public void testGetSRMReturningVoid() throws Exception {
        System.out.println("---\nAn issue should be reported if a get sub-resource method returns void:");
        AbstractResource ar = IntrospectionModeller.createResource(TestGetSRMReturningVoid.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }

    @Path("emptyResource")
    public static class TestEmptyResource {
        public void getSRMethod() {
        }
    }
    
    public void testEmptyResource() throws Exception {
        System.out.println("---\nAn issue should be reported if a resource does not contain any method neither any locator:");
        AbstractResource ar = IntrospectionModeller.createResource(TestEmptyResource.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }
    
    @Path("rootAmbigRMethods")
    public static class TestAmbigRMethods {
        
        @GET
        public String getIt() {
            return "it";
        }

        @GET
        public String getItWithParam(@QueryParam("q") String q) {
            return String.format("it, q=%s", q);
        }
    }

    public void ignoredTestAmbigRMethods() throws Exception {
        System.out.println("---\nAn issue should be reported if more than one method with same method designator and mime-types exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigRMethods.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }

    @Path("rootAmbigSRMethods")
    public static class TestAmbigSRMethods {
        
        @GET @Path("path")
        public String getIt() {
            return "it";
        }

        @GET @Path("path")
        public String getItWithParam(@QueryParam("q") String q) {
            return String.format("it, q=%s", q);
        }
    }

    public void ignoredTestAmbigSRMethods() throws Exception {
        System.out.println("---\nAn issue should be reported if more than one sub-resource method with same method designator and mime-types exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigSRMethods.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }
    

    private static void printIssueList(BasicValidator validator) {
        for (ResourceModelIssue issue : validator.getIssueList()) {
            System.out.println((issue.isFatal() ? "ERROR: " : "WARNING: ") + issue.getMessage());
        }
    }
}

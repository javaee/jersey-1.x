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
package com.sun.jersey.impl.modelapi.validation;

import com.sun.jersey.server.impl.modelapi.validation.BasicValidator;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.spi.resource.Singleton;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class BasicValidatorTest extends TestCase {

    @Path("rootNoCtor")
    public static class TestRootResourceWithoutPublicConstructor {

        private TestRootResourceWithoutPublicConstructor() { }

        @GET
        public String getIt() {
            return "it";
        }
    }

    public void testRootResourceWithoutPublicConstructor() throws Exception {
        System.out.println("---\nAn issue should be reported if a public ctor is missing at a root resource:");
        AbstractResource ar = IntrospectionModeller.createResource(TestRootResourceWithoutPublicConstructor.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootNonAmbigCtors")
    public static class TestRootResourceNonAmbigCtors {

        // TODO: hmmm, even if this is not ambiguous, it is strange; shall we warn, the 1st and the 2nd ctor won't be used?
        public TestRootResourceNonAmbigCtors(@QueryParam("s") String s) {}

        public TestRootResourceNonAmbigCtors(@QueryParam("n") int n) {}

        public TestRootResourceNonAmbigCtors(@QueryParam("s") String s, @QueryParam("n") int n) {}

        @GET
        public String getIt() {
            return "it";
        }
    }

    public void testRootResourceNonAmbigConstructors() throws Exception {
        System.out.println(
                "---\nNo issue should be reported if more public ctors exists with the same number of params, " +
                "but another just one is presented with more params at a root resource:");
        AbstractResource ar = IntrospectionModeller.createResource(TestRootResourceNonAmbigCtors.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.getIssueList().isEmpty());
    }

    @Singleton
    @Path("rootSingleton/{p}")
    public static class TestCantInjectFieldsForSingleton {

        @MatrixParam("m")
        String matrixParam;
        @QueryParam("q")
        String queryParam;
        @PathParam("p")
        String pParam;
        @CookieParam("c")
        String cParam;
        @HeaderParam("h")
        String hParam;

        @GET
        public String getIt() {
            return "it";
        }
    }

    // this should be sorted out at runtime rather than during validation
    public void suspendedTestSingletonFieldsInjection() throws Exception {
        System.out.println("---\nAn issue should be reported if injection is required for a singleton life-cycle:");
        AbstractResource ar = IntrospectionModeller.createResource(TestCantInjectFieldsForSingleton.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.getIssueList().isEmpty());
        assertEquals(5, validator.getIssueList().size());
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
        printIssueList(validator);
        assertTrue(!validator.getIssueList().isEmpty());
    // TODO: there might still be an implicit viewable associated with it
    // assertTrue(validator.getIssueList().get(0).isFatal());
    }

    public static class TestNonPublicRM1 {

        @GET
        private  String getThis() {
            return "this";
        }

        @PUT
        public void putThis(String t) {
        }
    }

    public void testNonPublicRM1() throws Exception {
        System.out.println("---\nAn issue should be reported if a resource method is not public:");
        AbstractResource ar = IntrospectionModeller.createResource(TestNonPublicRM1.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.getIssueList().isEmpty());
    }

    public static class TestMoreThanOneEntity {

        @PUT
        public void put(String one, String two) {
        }
    }

    // should be probably validated at runtime rather then at the validation phase
    public void suspendedTestMoreThanOneEntity() throws Exception {
        System.out.println("---\nAn issue should be reported if a resource method takes more than one entity params:");
        AbstractResource ar = IntrospectionModeller.createResource(TestMoreThanOneEntity.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.getIssueList().isEmpty());
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
        printIssueList(validator);
        assertTrue(!validator.getIssueList().isEmpty());
        assertTrue(!validator.getIssueList().get(0).isFatal());
    }

    public static class TestGetRMConsumingEntity {

        @GET
        public String getMethod(Object o) {
            return "it";
        }
    }

    public void testGetRMConsumingEntity() throws Exception {
        System.out.println("---\nAn issue should be reported if a get method consumes an entity:");
        AbstractResource ar = IntrospectionModeller.createResource(TestGetRMConsumingEntity.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
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
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    public static class TestGetSRMReturningVoid {

        @GET
        @Path("srm")
        public void getSRMethod() {
        }
    }

    public void testGetSRMReturningVoid() throws Exception {
        System.out.println("---\nAn issue should be reported if a get sub-resource method returns void:");
        AbstractResource ar = IntrospectionModeller.createResource(TestGetSRMReturningVoid.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.getIssueList().isEmpty());
        assertTrue(!validator.getIssueList().get(0).isFatal());
    }

    public static class TestGetSRMConsumingEntity {

        @Path("p")
        @GET
        public String getMethod(Object o) {
            return "it";
        }
    }

    public void testGetSRMConsumingEntity() throws Exception {
        System.out.println("---\nAn issue should be reported if a get method consumes an entity:");
        AbstractResource ar = IntrospectionModeller.createResource(TestGetSRMConsumingEntity.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootAmbigResourceMethodsGET")
    public static class TestAmbigResourceMethodsGET {

        @GET
        @Produces("application/xml")
        public String getXml() {
            return null;
        }

        @GET
        @Produces("text/plain")
        public String getText() {
            return "it";
        }

        @GET
        @Produces("text/plain")
        public String getTextWithParam(@QueryParam("q") String q) {
            return String.format("it, q=%s", q);
        }
    }

    public void testAmbigResourceMethodsGET() throws Exception {
        System.out.println("---\nAn issue should be reported for a resource method, if more than one HTTP method with the same output mime-types exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigResourceMethodsGET.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootAmbigResourceMethodsGET1")
    public static class TestAmbigResourceMethodsGET1 {

        @GET
        public String getXml() {
            return null;
        }

        @GET
        public String getText() {
            return "it";
        }
    }

    public void testAmbigResourceMethodsGET1() throws Exception {
        System.out.println("---\nAn issue should be reported for a resource method, if more than one HTTP method even with wilcard output mime-types exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigResourceMethodsGET1.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("/collection")
    @Produces("application/atom+xml")
    public static class FeedResource {

        @POST
        @Consumes("application/atom+xml")
        public Response postEntry(Object e) {
            return Response.noContent().build();
        }

        @POST
        public Response postMediaEntry(byte[] entry) {
            return null;
        }
    }

    public void testAmbigResourceMethodsPOSTFeed() throws Exception {
        System.out.println("---\nNo issue should be reported for a resource method, if more than one HTTP method with wilcard/non-wildcard input mime-types exist:");
        AbstractResource ar = IntrospectionModeller.createResource(FeedResource.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.getIssueList().isEmpty());
    }

    @Path("rootAmbigResourceMethodsPUT")
    public static class TestAmbigResourceMethodsPUT {

        @PUT
        @Consumes("application/xml")
        public void putXml(Object o) {
        }

        @PUT
        @Consumes({"text/plain", "image/jpeg"})
        public void putTextOrImg(Object o) {
        }

        @PUT
        @Consumes("text/plain")
        public void putTextWithParam(@QueryParam("q") String q, Object o) {
        }
    }

    public void testAmbigResourceMethodsPUT() throws Exception {
        System.out.println("---\nAn issue should be reported for a resource method, if more than one HTTP method with the same input mime-types exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigResourceMethodsPUT.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @HttpMethod("CUSTOM_HTTP_METHOD")
    public @interface CUSTOM_HTTP_METHOD {
    }

    @Path("rootAmbigResourceMethodsCUSTOM")
    public static class TestAmbigResourceMethodsCUSTOM {

        @CUSTOM_HTTP_METHOD
        @Consumes("application/xml")
        public void customXml(Object o) {
        }

        @CUSTOM_HTTP_METHOD
        @Consumes({"text/plain", "image/jpeg"})
        public void customTextOrImg(Object o) {
        }

        @CUSTOM_HTTP_METHOD
        @Consumes("text/plain")
        public void customTextWithParam(@QueryParam("q") String q, Object o) {
        }
    }

    public void testAmbigResourceMethodsCUSTOM() throws Exception {
        System.out.println("---\nAn issue should be reported for a resource method if more than one HTTP method with the same input mime-types exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigResourceMethodsCUSTOM.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootAmbigSubResourceMethodsGET")
    public static class TestAmbigSubResourceMethodsGET {

        @Path("{one}")
        @GET
        @Produces("application/xml")
        public String getXml() {
            return "{}";
        }

        @Path("{seven}")
        @GET
        @Produces("text/plain")
        public String getText() {
            return "it";
        }

        @Path("{million}")
        @GET
        @Produces("text/plain")
        public String getTextWithParam(@QueryParam("q") String q) {
            return String.format("it, q=%s", q);
        }
    }

    public void testAmbigSubResourceMethodsGET() throws Exception {
        System.out.println("---\nAn issue should be reported for a sub-resource method, if more than one HTTP method with the same output mime-type and uri path template exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigSubResourceMethodsGET.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootAmbigSubResourceMethodsPUT")
    public static class TestAmbigSubResourceMethodsPUT {

        @Path("sub/{one}")
        @PUT
        @Consumes("application/xml")
        public void putXml(Object o) {
        }

        @Path("sub/{slash}/")
        @PUT
        @Consumes({"text/plain", "image/jpeg"})
        public void putTextOrImg(Object o) {
        }

        @Path("sub/{two}")
        @PUT
        @Consumes("text/plain")
        public void putTextWithParam(@QueryParam("q") String q, Object o) {
        }
    }

    public void testAmbigSubResourceMethodsPUT() throws Exception {
        System.out.println("---\nAn issue should be reported for a sub-resource method, if more than one HTTP method with the same input mime-type and uri path template exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigSubResourceMethodsPUT.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootAmbigSubResourceMethodsCUSTOM")
    public static class TestAmbigSubResourceMethodsCUSTOM {

        @Path("sub/{a}")
        @CUSTOM_HTTP_METHOD
        @Consumes("application/xml")
        public void customGetXml(Object o) {
        }

        @Path("sub/{b}")
        @CUSTOM_HTTP_METHOD
        @Consumes({"text/plain", "image/jpeg"})
        public void customGetTextOrImg(Object o) {
        }

        @Path("sub/{c}")
        @CUSTOM_HTTP_METHOD
        @Consumes("text/plain")
        public void customGetTextWithParam(@QueryParam("q") String q, Object o) {
        }
    }

    public void testAmbigSubResourceMethodsCUSTOM() throws Exception {
        System.out.println("---\nAn issue should be reported for a sub-resource method, if more than one HTTP method with the same input mime-type and path exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigSubResourceMethodsCUSTOM.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootAmbigSRLocators")
    public static class TestAmbigSRLocators {

        @Path("{one}")
        public String locatorOne(@PathParam("one") String one) {
            return "it";
        }

        @Path("{two}")
        public String locatorTwo(@PathParam("two") String two, @QueryParam("q") String q) {
            return String.format("it, q=%s", q);
        }
    }

    public void testAmbigSRLocators() throws Exception {
        System.out.println("---\nAn issue should be reported if more than one sub-resource locator with the same path exists:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigSRLocators.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootAmbigSRLocatorsWithSlash")
    public static class TestAmbigSRLocatorsWithSlash {

        @Path("{one}")
        public String locatorOne(@PathParam("one") String one) {
            return "it";
        }

        @Path("{two}/")
        public String locatorTwo(@PathParam("two") String two, @QueryParam("q") String q) {
            return String.format("it, q=%s", q);
        }
    }

    public void testAmbigSRLocatorsWithSlash() throws Exception {
        System.out.println("---\nAn issue should be reported if more than one sub-resource locator with paths differing only in ending slash exist:");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbigSRLocatorsWithSlash.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootMultipleHttpMethodDesignatorsRM")
    public static class TestMultipleHttpMethodDesignatorsRM {

        @GET
        @PUT
        public String getPutIt() {
            return "it";
        }
    }

    public void testMultipleHttpMethodDesignatorsRM() throws Exception {
        System.out.println("---\nAn issue should be reported if more than one HTTP method designator exist on a resource method:");
        AbstractResource ar = IntrospectionModeller.createResource(TestMultipleHttpMethodDesignatorsRM.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootMultipleHttpMethodDesignatorsSRM")
    public static class TestMultipleHttpMethodDesignatorsSRM {

        @Path("srm")
        @POST
        @PUT
        public String postPutIt() {
            return "it";
        }
    }

    public void testMultipleHttpMethodDesignatorsSRM() throws Exception {
        System.out.println("---\nAn issue should be reported if more than one HTTP method designator exist on a sub-resource method:");
        AbstractResource ar = IntrospectionModeller.createResource(TestMultipleHttpMethodDesignatorsSRM.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path("rootEntityParamOnSRL")
    public static class TestEntityParamOnSRL {

        @Path("srl")
        public String locator(String s) {
            return "it";
        }
    }

    public void testEntityParamOnSRL() throws Exception {
        System.out.println("---\nAn issue should be reported if an entity parameter exists on a sub-resource locator:");
        AbstractResource ar = IntrospectionModeller.createResource(TestEntityParamOnSRL.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.fatalIssuesFound());
    }

    @Path(value = "/DeleteTest")
    public static class TestNonConflictingHttpMethodDelete {

        static String html_content =
                "<html>" + "<head><title>Delete text/html</title></head>" +
                "<body>Delete text/html</body></html>";

        @DELETE
        @Produces(value = "text/plain")
        public String getPlain() {
            return "Delete text/plain";
        }

        @DELETE
        @Produces(value = "text/html")
        public String getHtml() {
            return html_content;
        }

        @DELETE
        @Path(value = "/sub")
        @Produces(value = "text/html")
        public String getSub() {
            return html_content;
        }
    }

    public void testNonConflictingHttpMethodDelete() throws Exception {
        System.out.println("---\nNo issue should be reported if produced mime types differ");
        AbstractResource ar = IntrospectionModeller.createResource(TestNonConflictingHttpMethodDelete.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(validator.getIssueList().isEmpty());
    }

    @Path(value = "/AmbigParamTest")
    public static class TestAmbiguousParams {

        @QueryParam("q")
        @HeaderParam("q")
        private int a;

        @QueryParam("b")
        @HeaderParam("b")
        @MatrixParam("q")
        public void setB(String b) {
        }

        @GET @Path("a")
        public String get(@PathParam("a") @QueryParam("a") String a) {
            return "hi";
        }

        @GET @Path("b")
        public String getSub(@PathParam("a") @QueryParam("b") @MatrixParam("c") String a, @MatrixParam("m") @QueryParam("m") int i) {
            return "hi";
        }

        @Path("c")
        public Object getSubLoc(@MatrixParam("m") @CookieParam("c") String a) {
            return null;
        }
    }

    public void testAmbiguousParams() throws Exception {
        System.out.println("---\nA warning should be reported if ambiguous source of a parameter is seen");
        AbstractResource ar = IntrospectionModeller.createResource(TestAmbiguousParams.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.fatalIssuesFound());
        assertEquals(6, validator.getIssueList().size());
    }

    @Path(value = "/EmptyPathSegmentTest")
    public static class TestEmptyPathSegment {

        @GET @Path("/")
        public String get() {
            return "hi";
        }
    }

    public void testEmptyPathSegment() throws Exception {
        System.out.println("---\nA warning should be reported if @Path with \"/\" or empty string value is seen");
        AbstractResource ar = IntrospectionModeller.createResource(TestEmptyPathSegment.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.fatalIssuesFound());
        assertEquals(1, validator.getIssueList().size());
    }



    public static class TypeVariableResource<T, V> {
        @QueryParam("v") V fieldV;

        V methodV;

        @QueryParam("v")
        public void set(V methodV) {
            this.methodV = methodV;
        }

        @GET
        public String get(@QueryParam("v") V getV) {
            return getV.toString() + fieldV.toString() + methodV.toString();
        }

        @POST
        public T post(T t) {
            return t;
        }

        @Path("sub")
        @POST
        public T postSub(T t) {
            return t;
        }
    }

    public void testTypeVariableResource() throws Exception {
        System.out.println("---\n");
        AbstractResource ar = IntrospectionModeller.createResource(TypeVariableResource.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.fatalIssuesFound());
        assertEquals(7, validator.getIssueList().size());
    }

    public static class ParameterizedTypeResource<T, V> {
        @QueryParam("v") Collection<V> fieldV;

        List<List<V>> methodV;

        @QueryParam("v")
        public void set(List<List<V>> methodV) {
            this.methodV = methodV;
        }

        @GET
        public String get(@QueryParam("v") Map<String, List<V>> getV) {
            return "";
        }

        @POST
        public Collection<T> post(Collection<T> t) {
            return t;
        }

        @Path("sub")
        @POST
        public Collection<T> postSub(Collection<T> t) {
            return t;
        }
    }

    public static class ConcreteParameterizedTypeResource extends ParameterizedTypeResource<String, String> {
    }

    public void testParameterizedTypeResource() throws Exception {
        System.out.println("---\n");
        AbstractResource ar = IntrospectionModeller.createResource(ConcreteParameterizedTypeResource.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.fatalIssuesFound());
        assertEquals(7, validator.getIssueList().size());
    }

    public static class GenericArrayResource<T, V> {
        @QueryParam("v") V[] fieldV;

        V[] methodV;

        @QueryParam("v")
        public void set(V[] methodV) {
            this.methodV = methodV;
        }

        @GET
        public String get(@QueryParam("v") V[] getV) {
            return "";
        }

        @POST
        public T[] post(T[] t) {
            return t;
        }

        @Path("sub")
        @POST
        public T[] postSub(T[] t) {
            return t;
        }
    }

    public static class ConcreteGenericArrayResource extends GenericArrayResource<String, String> {
    }

    public void testGenericArrayResource() throws Exception {
        System.out.println("---\n");
        AbstractResource ar = IntrospectionModeller.createResource(ConcreteGenericArrayResource.class);
        BasicValidator validator = new BasicValidator();
        validator.validate(ar);
        printIssueList(validator);
        assertTrue(!validator.fatalIssuesFound());
        assertEquals(7, validator.getIssueList().size());
    }

    // TODO: test multiple root resources with the same uriTempl (in WebApplicationImpl.processRootResources ?)

    private static void printIssueList(BasicValidator validator) {
        for (ResourceModelIssue issue : validator.getIssueList()) {
            System.out.println((issue.isFatal() ? "ERROR: " : "WARNING: ") + issue.getMessage());
        }
    }
}

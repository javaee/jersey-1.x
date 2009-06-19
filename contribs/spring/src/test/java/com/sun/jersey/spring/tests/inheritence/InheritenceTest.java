/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.spring.tests.inheritence;

import com.sun.jersey.spring.tests.*;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.springframework.stereotype.Component;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author paulsandoz
 */
@Test
public class InheritenceTest extends AbstractTest {

    @Path("a")
    @Component
    public static class A {
        @GET
        public String get() {
            return "a";
        }

        @Path("sub")
        public ASub getSub() {
            return new ASub();
        }
    }

    @Path("b")
    @Component
    public static class B extends A {
        @GET
        @Override
        public String get() {
            return "b";
        }

        @Path("sub")
        @Override
        public BSub getSub() {
            return new BSub();
        }
    }

    @Path("b2")
    @Component
    public static class B2 extends A {
        @GET
        @Override
        public String get() {
            return "b2";
        }

        @Path("sub")
        @Override
        public B2Sub getSub() {
            return new B2Sub();
        }
    }

    @Path("c")
    @Component
    public static class C extends B {
        @GET
        @Override
        public String get() {
            return "c";
        }

        @Path("sub")
        @Override
        public CSub getSub() {
            return new CSub();
        }
    }

    @Component
    public static class ASub {
        @GET
        public String get() {
            return "asub";
        }
    }

    @Component
    public static class BSub extends ASub {
        @GET
        @Override
        public String get() {
            return "bsub";
        }
    }

    @Component
    public static class B2Sub extends ASub {
        @GET
        @Override
        public String get() {
            return "b2sub";
        }
    }

    @Component
    public static class CSub extends BSub {
        @GET
        @Override
        public String get() {
            return "csub";
        }
    }

    @Test
    public void testInheritence() {
        start();

        WebResource r = resource("a");
        Assert.assertEquals("a", r.get(String.class));

        r = resource("b");
        Assert.assertEquals("b", r.get(String.class));

        r = resource("b2");
        Assert.assertEquals("b2", r.get(String.class));

        r = resource("c");
        Assert.assertEquals("c", r.get(String.class));
    }

    @Test
    public void testSubInheritence() {
        start();

        WebResource r = resource("a/sub");
        Assert.assertEquals("asub", r.get(String.class));

        r = resource("b/sub");
        Assert.assertEquals("bsub", r.get(String.class));

        r = resource("b2/sub");
        Assert.assertEquals("b2sub", r.get(String.class));

        r = resource("c/sub");
        Assert.assertEquals("csub", r.get(String.class));
    }
}

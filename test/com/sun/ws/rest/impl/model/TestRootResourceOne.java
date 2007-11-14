/*
 * TestRootResourceOne.java
 *
 * Created on November 5, 2007, 11:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.model;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.UriTemplate;

/**
 *
 * @author japod
 */
@UriTemplate("/one")
public class TestRootResourceOne {
    
    /** Creates a new instance of TestRootResourceOne */
    public TestRootResourceOne() {
    }
    
    @HttpMethod
    public String getResourceMethodTester() {
        return "Hi there, here is a resource method.";
    }
    
    @UriTemplate("/subres-locator")
    public TestSubResourceOne getSubResourceMethodTester() {
        return new TestSubResourceOne();
    }

    @HttpMethod
    @UriTemplate("/subres-method")
    public String getSubResourceMethod() {
        return "Hi there, here is a subresource method!";
    }

}

/*
 * TestSubResourceOne.java
 *
 * Created on November 5, 2007, 11:55 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.model;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.HttpMethod;

/**
 *
 * @author japod
 */
public class TestSubResourceOne {
    
    /** Creates a new instance of TestSubResourceOne */
    public TestSubResourceOne() {
    }
    
    @HttpMethod
    public String getResourceMethodTester() {
        return "hi, here is a resource method of TestSubResourceOne";
    }
    @HttpMethod
    @ConsumeMime("text/plain")
    public String putResourceMethodTester() {
        return "hi, here is a put resource method of TestSubResourceOne";
    }
    
}

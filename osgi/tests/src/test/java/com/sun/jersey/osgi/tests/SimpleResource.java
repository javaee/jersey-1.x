/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.osgi.tests;


import javax.ws.rs.*;
/**
 *
 * @author japod
 */
@Path("/simple")
public class SimpleResource {

    @GET public String getMe() {
        return "OK";
    }
}

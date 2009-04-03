/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.samples.https_grizzly.resource;

import com.sun.jersey.samples.https_grizzly.Server;
import com.sun.jersey.samples.https_grizzly.auth.Base64;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author pavel.bucek@sun.com
 */
@Path("/")
public class RootResource {

    @GET
    public Response get1(@Context HttpHeaders headers) {
        // you can get username form HttpHeaders
        System.out.println("Service: GET / User: " + getUser(headers));

        return Response.ok(Server.CONTENT).type(MediaType.TEXT_HTML).build();
    }

    private String getUser(HttpHeaders headers) {

        // this is a very minimalistic and "naive" code; if you plan to use it
        // add necessary checks (see com.sun.jersey.samples.https_grizzly.auth.SecurityFilter)

        String auth = headers.getRequestHeader("authorization").get(0);

        auth = auth.substring("Basic ".length());
        String[] values = new String(Base64.base64Decode(auth)).split(":");

        // String username = values[0];
        // String password = values[1];

        return values[0];
    }
}

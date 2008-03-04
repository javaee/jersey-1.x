/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.methodparams;

import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class EncodedParamsTest extends AbstractResourceTester {

    public EncodedParamsTest(String testName) {
        super(testName);
    }

    @Encoded
    @Path("/{u}")
    public static class EncodedOnClass {
        public EncodedOnClass(
                @PathParam("u") String u,
                @QueryParam("q") String q, 
                @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
        }
        
        @GET
        public String doGet(
                @PathParam("u") String u,
                @QueryParam("q") String q, 
                @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
            return "content";
        }
    }
            
    public void testEncodedOnClass() {
        initiateWebApplication(EncodedOnClass.class);
        
        resource("/%20u;m=%20m?q=%20q").get(String.class);
    }
    
    @Path("/{u}")
    public static class EncodedOnAccessibleObject {
        @Encoded
        public EncodedOnAccessibleObject(
                @PathParam("u") String u,
                @QueryParam("q") String q, 
                @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
        }
        
        @Encoded
        @GET
        public String doGet(
                @PathParam("u") String u,
                @QueryParam("q") String q, 
                @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
            return "content";
        }
    }
    
    public void testEncodedOnAccessibleObject() {
        initiateWebApplication(EncodedOnAccessibleObject.class);
        
        resource("/%20u;m=%20m?q=%20q").get(String.class);
    }
    
    @Path("/{u}")
    public static class EncodedOnParameters {
        public EncodedOnParameters(
                @Encoded @PathParam("u") String u,
                @Encoded @QueryParam("q") String q, 
                @Encoded @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
        }
        
        @GET
        public String doGet(
                @Encoded @PathParam("u") String u,
                @Encoded @QueryParam("q") String q, 
                @Encoded @MatrixParam("m") String m) {
            assertEquals("%20u", u);
            assertEquals("%20q", q);
            assertEquals("%20m", m);
            return "content";
        }
    }

    public void testEncodedOnParameters() {
        initiateWebApplication(EncodedOnParameters.class);
        
        resource("/%20u;m=%20m?q=%20q").get(String.class);
    }

    @Path("/{u}")
    public static class MixedEncodedOnParameters {
        public MixedEncodedOnParameters(
                @PathParam("u") String du,
                @QueryParam("q") String dq, 
                @MatrixParam("m") String dm,                
                @Encoded @PathParam("u") String eu,
                @Encoded @QueryParam("q") String eq, 
                @Encoded @MatrixParam("m") String em) {
            assertEquals(" u", du);
            assertEquals(" q", dq);
            assertEquals(" m", dm);
            assertEquals("%20u", eu);
            assertEquals("%20q", eq);
            assertEquals("%20m", em);
        }
        
        @GET
        public String doGet(
                @PathParam("u") String du,
                @QueryParam("q") String dq, 
                @MatrixParam("m") String dm,                
                @Encoded @PathParam("u") String eu,
                @Encoded @QueryParam("q") String eq, 
                @Encoded @MatrixParam("m") String em) {
            assertEquals(" u", du);
            assertEquals(" q", dq);
            assertEquals(" m", dm);
            assertEquals("%20u", eu);
            assertEquals("%20q", eq);
            assertEquals("%20m", em);
            return "content";
        }
    }
    
    public void testMixedEncodedOnParameters() {
        initiateWebApplication(MixedEncodedOnParameters.class);
        
        resource("/%20u;m=%20m?q=%20q").get(String.class);
    }
}

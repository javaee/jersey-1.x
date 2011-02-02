/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.samples.multipart.resources;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

// The Java class will be hosted at the URI path "/helloworld"
@Path("/form")
public class MultipartResource {

    @Path("part")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    public String post(@FormDataParam("part") String s) {
        return s;
    }

    @Path("part-file-name")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    public String post(
            @FormDataParam("part") String s,
            @FormDataParam("part") FormDataContentDisposition d) {
        return s + ":" + d.getFileName();
    }

    @Path("xml-jaxb-part")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @POST
    public String post(
            @FormDataParam("string") String s,
            @FormDataParam("string") FormDataContentDisposition sd,
            @FormDataParam("bean") Bean b,
            @FormDataParam("bean") FormDataContentDisposition bd
            ) {
        return s + ":" + sd.getFileName() + "," + b.value + ":" + bd.getFileName();
    }

}
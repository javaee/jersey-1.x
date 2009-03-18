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

package com.sun.jersey.multipart.impl;

import com.sun.jersey.multipart.BodyPart;
import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.multipart.MultiPart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;

/**
 * <p>Resource file for {@link MultiPartReaderWriterTest}.</p>
 */
@Path("/multipart")
public class MultiPartResource {

    @Context
    private Providers providers = null;

    @Path("zero")
    @GET
    @Produces("text/plain")
    public String zero() {
        return "Hello, world\r\n";
    }

    @Path("one")
    @GET
    @Produces("multipart/mixed")
    public Response one() {
        MultiPart entity = new MultiPart();
        // Exercise manually adding part(s) to the bodyParts property
        BodyPart part = new BodyPart("This is the only segment", new MediaType("text", "plain"));
        entity.getBodyParts().add(part);
        return Response.ok(entity).type("multipart/mixed").build();
    }

    @Path("two")
    @GET
    @Produces("multipart/mixed")
    public Response two() {
        // Exercise builder pattern with default content type
        return Response.ok(new MultiPart().
                             bodyPart("This is the first segment", new MediaType("text", "plain")).
                             bodyPart("<outer><inner>value</inner></outer>", new MediaType("text", "xml"))).build();
    }

    @Path("three")
    @GET
    @Produces("multipart/mixed")
    public Response three() {
        // Exercise builder pattern with explicit content type
        MultiPartBean bean = new MultiPartBean("myname", "myvalue");
        return Response.ok(new MultiPart().
                             type(new MediaType("multipart", "mixed")).
                             bodyPart("This is the first segment", new MediaType("text", "plain")).
                             bodyPart(bean, new MediaType("x-application", "x-format"))).build();
    }

    @Path("four")
    @PUT
    @Produces("text/plain")
    public Response four(MultiPart multiPart) {
        if (!(multiPart.getBodyParts().size() == 2)) {
            return Response.ok("FAILED:  Number of body parts is " + multiPart.getBodyParts().size() + " instead of 2").build();
        }
        BodyPart part0 = multiPart.getBodyParts().get(0);
        if (!(part0.getMediaType().equals(new MediaType("text", "plain")))) {
            return Response.ok("FAILED:  First media type is " + part0.getMediaType()).build();
        }
        BodyPart part1 = multiPart.getBodyParts().get(1);
        if (!(part1.getMediaType().equals(new MediaType("x-application", "x-format")))) {
            return Response.ok("FAILED:  Second media type is " + part1.getMediaType()).build();
        }
        MultiPartBean bean = part1.getEntityAs(MultiPartBean.class);
        if (!bean.getName().equals("myname")) {
            return Response.ok("FAILED:  Second part name = " + bean.getName()).build();
        }
        if (!bean.getValue().equals("myvalue")) {
            return Response.ok("FAILED:  Second part value = " + bean.getValue()).build();
        }
        return Response.ok("SUCCESS:  All tests passed").build();
    }

    // Test "multipart/form-data" the hard way (no subclasses)
    @Path("five")
    @GET
    @Produces("multipart/form-data")
    public Response five() {
        MultiPart entity = new MultiPart();
        entity.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        BodyPart part1 = new BodyPart();
        part1.setMediaType(MediaType.TEXT_PLAIN_TYPE);
        part1.getHeaders().add("Content-Disposition", "form-data; name=\"field1\"");
        part1.setEntity("Joe Blow\r\n");
        BodyPart part2 = new BodyPart();
        part2.setMediaType(MediaType.TEXT_PLAIN_TYPE);
        part2.getHeaders().add("Content-Disposition", "form-data; name=\"pics\"; filename=\"file1.txt\"");
        part2.setEntity("... contents of file1.txt ...\r\n");
        return Response.ok(entity.bodyPart(part1).bodyPart(part2)).build();
    }

    // Note - this should never actually get reached, because the client
    // is trying to post a MultiPart with no body parts inside, and that
    // should throw a client side exception
    @Path("six")
    @POST
    @Consumes("multipart/mixed")
    @Produces("text/plain")
    public Response six(MultiPart multiPart) {
        String response = "All OK";
        if (!"multipart".equals(multiPart.getMediaType().getType()) ||
            !"mixed".equals(multiPart.getMediaType().getSubtype())) {
            response = "MultiPart media type is " + multiPart.getMediaType().toString();
        } else if (multiPart.getBodyParts().size() != 0) {
            response = "Got " + multiPart.getBodyParts().size() + " body parts instead of zero";
        }
        return Response.ok(response).build();
    }

    // Test "multipart/form-data" the easy way (with subclasses)
    @Path("seven")
    @GET
    @Produces("multipart/form-data")
    public Response seven() {
        // Exercise builder pattern with explicit content type
        MultiPartBean bean = new MultiPartBean("myname", "myvalue");
        return Response.ok(new FormDataMultiPart().
                             field("foo", "bar").
                             field("baz", "bop").
                             field("bean", bean, new MediaType("x-application", "x-format"))).build();
    }

    @Path("eight")
    @PUT
    @Consumes("multipart/form-data")
    @Produces("text/plain")
    public Response eight(FormDataMultiPart multiPart) {
        if (!(multiPart.getBodyParts().size() == 3)) {
            return Response.ok("FAILED:  Number of body parts is " + multiPart.getBodyParts().size() + " instead of 3").build();
        }
        if (multiPart.getField("foo") == null) {
            return Response.ok("FAILED:  Missing field 'foo'").build();
        } else if (!"bar".equals(multiPart.getField("foo").getValue())) {
            return Response.ok("FAILED:  Field 'foo' has value '" + multiPart.getField("foo").getValue() + "' instead of 'bar'").build();
        }
        if (multiPart.getField("baz") == null) {
            return Response.ok("FAILED:  Missing field 'baz'").build();
        } else if (!"bop".equals(multiPart.getField("baz").getValue())) {
            return Response.ok("FAILED:  Field 'baz' has value '" + multiPart.getField("baz").getValue() + "' instead of 'bop'").build();
        }
        if (multiPart.getField("bean") == null) {
            return Response.ok("FAILED:  Missing field 'bean'").build();
        }
        MultiPartBean bean = multiPart.getField("bean").getValueAs(MultiPartBean.class);
        if (!bean.getName().equals("myname")) {
            return Response.ok("FAILED:  Second part name = " + bean.getName()).build();
        }
        if (!bean.getValue().equals("myvalue")) {
            return Response.ok("FAILED:  Second part value = " + bean.getValue()).build();
        }
        return Response.ok("SUCCESS:  All tests passed").build();
    }

    @Path("nine")
    @PUT
    @Consumes("multipart/form-data")
    @Produces("text/plain")
    public Response nine(
            @FormDataParam("foo") String foo,
            @FormDataParam("baz") String baz,
            @FormDataParam("unknown1") String unknown1,
            @FormDataParam("unknown2") @DefaultValue("UNKNOWN") String unknown2,
            FormDataMultiPart fdmp) {

        if (!"bar".equals(foo)) {
            return Response.ok("FAILED:  Value of 'foo' is '" + foo + "' instead of 'bar'").build();
        } else if (!"bop".equals(baz)) {
            return Response.ok("FAILED:  Value of 'baz' is '" + baz + "' instead of 'bop'").build();
        } else if (unknown1 != null) {
            return Response.ok("FAILED:  Value of 'unknown1' is '" + unknown1 + "' instead of NULL").build();
        } else if (!"UNKNOWN".equals(unknown2)) {
            return Response.ok("FAILED:  Value of 'unknown2' is '" + unknown2 + "' instead of 'UNKNOWN'").build();
        } else if (fdmp == null) {
            return Response.ok("FAILED:  Value of fdmp is NULL").build();
        } else if (fdmp.getFields().size() != 3) {
            return Response.ok("FAILED:  Value of fdmp.getFields().size() is " + fdmp.getFields().size() + " instead of 3").build();
        }
        return Response.ok("SUCCESS:  All tests passed").build();
    }

    @Path("ten")
    @PUT
    @Consumes("multipart/mixed")
    @Produces("text/plain")
    public Response ten(MultiPart mp) {
        if (!(mp.getBodyParts().size() == 2)) {
            return Response.ok("FAILED:  Body part count is " + mp.getBodyParts().size() + " instead of 2").build();
        } else if (!(mp.getBodyParts().get(1).getEntity() instanceof BodyPartEntity)) {
            return Response.ok("FAILED:  Second body part is " + mp.getBodyParts().get(1).getClass().getName() + " instead of BodyPartEntity").build();
        }
        BodyPartEntity bpe = (BodyPartEntity) mp.getBodyParts().get(1).getEntity();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream stream = bpe.getInputStream();
            byte[] buffer = new byte[2048];
            while (true) {
                int n = stream.read(buffer);
                if (n < 0) {
                    break;
                }
                baos.write(buffer, 0, n);
            }
            if (baos.toByteArray().length > 0) {
                return Response.ok("FAILED:  Second body part had " + baos.toByteArray().length + " bytes instead of 0").build();
            }
            return Response.ok("SUCCESS:  All tests passed").build();
        } catch (IOException e) {
            return Response.ok("FAILED:  Threw IOException").build();
        }
    }

    // Echo back a body part whose content may or may not exceed the size
    // of the buffer threshold
    @Path("eleven")
    @PUT
    @Consumes("multipart/mixed")
    @Produces("multipart/mixed")
    public Response eleven(MultiPart multiPart) throws IOException {
        BodyPartEntity bpe = (BodyPartEntity) multiPart.getBodyParts().get(0).getEntity();
        StringBuilder sb = new StringBuilder();
        InputStream stream = bpe.getInputStream();
        InputStreamReader reader = new InputStreamReader(stream);
        char[] buffer = new char[2048];
        while (true) {
            int n = reader.read(buffer);
            if (n < 0) {
                break;
            }
            sb.append(buffer, 0, n);
        }
        return Response.ok(new MultiPart().bodyPart(sb.toString(), MediaType.TEXT_PLAIN_TYPE)).
                type(new MediaType("multipart", "mixed")).build();
    }


    // Echo back the multipart that was sent
    @Path("twelve")
    @PUT
    @Consumes("multipart/mixed")
    @Produces("multipart/mixed")
    public MultiPart twelve(MultiPart multiPart) throws IOException {
        return multiPart;
    }

    // Call clean up explicitly
    @Path("thirteen")
    @PUT
    @Consumes("multipart/mixed")
    @Produces("multipart/mixed")
    public String thirteen(MultiPart multiPart) throws IOException {
        multiPart.cleanup();
        return "cleanup";
    }
}

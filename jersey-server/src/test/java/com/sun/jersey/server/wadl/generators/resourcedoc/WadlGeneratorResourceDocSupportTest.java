/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.wadl.generators.resourcedoc;

import java.io.StringWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;

import javax.ws.rs.POST;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.WadlBuilder;
import com.sun.jersey.server.wadl.WadlGenerator;
import com.sun.jersey.server.wadl.WadlGeneratorImpl;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.AnnotationDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ClassDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.MethodDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.NamedValueType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ParamDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ResourceDocType;
import com.sun.research.ws.wadl.Application;

import org.junit.Test;

public class WadlGeneratorResourceDocSupportTest
{
    @Test
    public void wadlIsGeneratedWithUnknownCustomParameterAnnotation() throws JAXBException
    {
        /* Set up a ClassDocType that has something for a custom-annotated parameter */
        ClassDocType cdt = new ClassDocType();
        cdt.setClassName(TestResource.class.getName());

        MethodDocType mdt = new MethodDocType();
        mdt.setMethodName("method");
        cdt.getMethodDocs().add(mdt);

        ParamDocType pdt = new ParamDocType("x", "comment about x");
        mdt.getParamDocs().add(pdt);

        AnnotationDocType adt = new AnnotationDocType();
        adt.setAnnotationTypeName(CustomParam.class.getName());
        adt.getAttributeDocs().add(new NamedValueType("value", "x"));

        pdt.getAnnotationDocs().add(adt);

        ResourceDocType rdt = new ResourceDocType();
        rdt.getDocs().add(cdt);


        /* Generate WADL for that class */
        WadlGenerator wg = new WadlGeneratorResourceDocSupport(new WadlGeneratorImpl(), rdt);

        WadlBuilder wb = new WadlBuilder(wg);
        AbstractResource resource = IntrospectionModeller.createResource(TestResource.class);
        ApplicationDescription app = wb.generate(null,null,null, Collections.singleton(resource));


        /* Confirm that it can be marshalled without error */
        StringWriter sw = new StringWriter();

        JAXBContext context = JAXBContext.newInstance(Application.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(app.getApplication(), sw);
    }

    public static class TestResource
    {
        @POST
        public String method(@CustomParam("x") Object param)
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * An annotation IntrospectionModeller doesn't know about.
     */
    @Target(ElementType.PARAMETER)
    @Retention (RetentionPolicy.RUNTIME)
    public @interface CustomParam
    {
        String value();
    }
}
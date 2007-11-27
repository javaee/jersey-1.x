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

package com.sun.ws.rest.tools.wadl.resource;

import static com.sun.codemodel.ClassType.CLASS;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import com.sun.ws.rest.impl.wadl.WadlReader;
import com.sun.ws.rest.tools.FilerCodeWriter;
import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;

/**
 * Generate a resource class that produces the WADL file for a GET request
 */
public class WadlResourceGenerator {
    
    private static final String RESOURCE_PACKAGE = "com.sun.ws.rest.wadl.resource";
    private static final String WADL_MEDIA_TYPE = "application/vnd.sun.wadl+xml";
    private static final String WADL_RESOURCE_CLASS = "WadlResource";
    private JCodeModel cm;
    private AnnotationProcessorContext context;
    private String pkg;

    /** Creates a new instance of WadlResourceGenerator */
    public WadlResourceGenerator(AnnotationProcessorContext context, String pkg) {
        this.cm = new JCodeModel();
        this.context = context;
        if (pkg == null)
            this.pkg = RESOURCE_PACKAGE;
        else
            this.pkg = pkg;
    }

    public String getPackage() {
        return pkg;
    }

    public void generate() {
        StringBuilder b = new StringBuilder();
        b.append(pkg);
        if (pkg.length()>0 && !pkg.endsWith("."))
            b.append(".");
        b.append(WADL_RESOURCE_CLASS);
        String className = b.toString();
        try {
            // create class
            CodeWriter cw = new FilerCodeWriter(context.getSourceDir(), context.getFiler());
            JDefinedClass cls = getWadlResourceClass(className, CLASS);
            JDocComment doc = cls.javadoc();
            doc.add("This class was generated.\n\n");
            doc.add("It is used to retrieve a WADL description\n");
            doc.add("of all of the other resources\n");
            // annotations
            cls.annotate(ProduceMime.class).param("value", WADL_MEDIA_TYPE);
            cls.annotate(Path.class).param("value", "/application.wadl");
            // fields
            JFieldVar uriInfo = cls.field(JMod.PUBLIC, UriInfo.class, "uriInfo");
            uriInfo.annotate(HttpContext.class);
            // methods
            JClass string = cm.ref(String.class);
            JClass inputStream = cm.ref(InputStream.class);
            JClass wadlReader = cm.ref(WadlReader.class);
            JMethod getWadl = cls.method(JMod.PUBLIC, string, "getWadl");
            getWadl.annotate(HttpMethod.class).param("value","GET");
            JBlock body = getWadl.body();
            JVar is = body.decl(inputStream, "is", JExpr._this().invoke("getClass").invoke("getResourceAsStream").arg("application.wadl"));
            JVar str = body.decl(cm.ref(String.class), "str", wadlReader.staticInvoke("read").arg(is).arg(uriInfo.invoke("getBaseUri")));
            // TODO patch baseURI in WADL to that of Servlet root
            body._return(str);
            cm.build(cw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected JDefinedClass getWadlResourceClass(String className, com.sun.codemodel.ClassType type) {
        JDefinedClass cls;
        try {
            cls = cm._class(className, type);
        } catch (com.sun.codemodel.JClassAlreadyExistsException e){
            cls = cm._getClass(className);
        }
        return cls;
    }

}

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

package com.sun.ws.rest.tools.webapp;

import static com.sun.codemodel.ClassType.CLASS;
import com.sun.codemodel.*;
import com.sun.ws.rest.tools.FilerCodeWriter;
import com.sun.ws.rest.tools.Messager;
import com.sun.ws.rest.tools.ToolsMessages;
import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import com.sun.ws.rest.api.core.ResourceConfig;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class generates a class that implements
 * <code>ResourceConfig</code> which will hold the
 * Resource classes that are to be part of a web application.
 * 
 * 
 * @author Doug Kohlert
 */
public class WebResourcesGenerator {
    /**
     * Global <code>JCodeModel</code> used for code generation
     */
    protected JCodeModel cm;
    
    /**
     * Context uses to store invormation about current state
     */
    protected AnnotationProcessorContext context;
    
    /**
     * Constant value for the default package for the generated 
     * <code>WebResources</code>.
     */
    private static final String RESOURCE_PACKAGE = "webresources.";
    /**
     * Constant value for the ame of the resource class to be generated
     */
    private static final String WEB_RESOURCES_CLASS = "WebResources";
    
    /**
     * <code>Messager</code> object use to report errors and warnings.
     */
    private Messager messager;
    
    /**
     * Creates a new instance of WebResourcesGenerator
     */
    public WebResourcesGenerator(Messager messager, AnnotationProcessorContext context) {
        this.messager = messager;
        this.context = context;
    }
    
    public void generateResourceClassSet(String pkg) {
        cm = new JCodeModel();
        if (pkg == null)
            pkg = RESOURCE_PACKAGE;
        else if (pkg.length()>0 && !pkg.endsWith("."))
            pkg += ".";
        String className = pkg + WEB_RESOURCES_CLASS;
        context.setResourceBeanClassName(className);
        try {
            CodeWriter cw = new FilerCodeWriter(context.getSourceDir(), context.getFiler());
            JDefinedClass cls = getCMClass(className, CLASS);
            cls._implements(ResourceConfig.class);
            JDocComment doc = cls.javadoc();
            doc.add("This class was generated.\n\n");
            doc.add("It is used to retrieve a Set<Class>\n");
            doc.add("where each <code>Class</code> in the <code>Set</code>\n");
            doc.add("represents a Web resource.");
            // members
            JClass setClass = cm.ref(Set.class).narrow(Class.class);
            JClass hs = cm.ref(HashSet.class).narrow(Class.class);
            JFieldVar field = cls.field(JMod.PRIVATE, setClass, "resources");
            field.init(JExpr._new(hs));
            
            //Constructor
            JMethod constrc1 = cls.constructor(JMod.PUBLIC);
            doc = constrc1.javadoc();
            doc.add("Initializes the Set of web resource classes\n");
            doc.add("to be included in a web application.");
            JBlock cb1 = constrc1.body();
            
            for (String clazz : context.getResourceClasses()) {
                cb1.directStatement("resources.add("+clazz+".class);");
            }
            
            JMethod method = cls.method(JMod.PUBLIC, setClass, "getResourceClasses");
            JBlock body = method.body();
            body._return(field);
            
            method = cls.method(JMod.PUBLIC, cm.BOOLEAN, "isIgnoreMatrixParams");
            body = method.body();
            body._return(JExpr.TRUE);
            
            method = cls.method(JMod.PUBLIC, cm.BOOLEAN, "isRedirectToNormalizedURI");
            body = method.body();
            body._return(JExpr.TRUE);
            
//            if(options.verbose)
//                cw = new ProgressCodeWriter(cw, System.out);
            
            cm.build(cw);
        } catch (IOException e) {
            messager.reportError(ToolsMessages.NESTED_ERROR(e.getLocalizedMessage()));
        }
        
    }
    
    protected JDefinedClass getCMClass(String className, com.sun.codemodel.ClassType type) {
        JDefinedClass cls;
        try {
            cls = cm._class(className, type);
        } catch (com.sun.codemodel.JClassAlreadyExistsException e){
            cls = cm._getClass(className);
        }
        return cls;
    }
    
}

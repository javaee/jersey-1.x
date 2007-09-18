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

package com.sun.ws.rest.tools.annotation.apt;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;
import com.sun.mirror.util.SimpleTypeVisitor;
import com.sun.mirror.util.TypeVisitor;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.model.method.ResourceHttpMethod;
import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import com.sun.ws.rest.tools.annotation.Method;
import com.sun.ws.rest.tools.annotation.Param;
import com.sun.ws.rest.tools.annotation.Resource;
import com.sun.ws.rest.tools.wadl.resource.WadlResourceGenerator;
import com.sun.ws.rest.tools.wadl.writer.WadlWriter;
import com.sun.ws.rest.tools.webapp.WebResourcesGenerator;
import com.sun.ws.rest.tools.webapp.writer.WebAppWriter;
import com.sun.ws.rest.tools.Messager;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.UriTemplate;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.xml.namespace.QName;
 

/**
 *
 * @author Doug Kohlert
 */

public class UriTemplateProcessor implements Messager, AnnotationProcessor {
    
    AnnotationProcessorEnvironment apEnv;
    
    protected AnnotationProcessorContext context;
    
    /**
     * The Base URL used for the WebApplication
     * This will be used to generated the &lt;url-pattern> in the web.xml
     */
    String urlPattern = null;
    
    /**
     * The Class name of the servlet
     * This will be used to generated the &lt;url-pattern> in the web.xml
     */
    String servletClassName = null;
    
    /**
     * The WebApplication name.
     * This will be used as the &lt;servlet-name> in the generated web.xml
     */
    String servletName = null;
    
    /**
     * The package that the generated <code>WebResouces</code>
     * glass should be generated into
     */
    String webresourcesPackage = "webresources";
    
    /**
     * Determines if web.xml should be generated
     */
    boolean generateWebXml = false;
    
    /**
     * Determines if wadl should be generated
     */
    boolean generateWadl = true;
    
    /**
     * Determines if request should be redirected or silently forwarded
     */
    boolean redirect = true;
    
    /**
     * Determines if URI path should be canonicalized
     */
    boolean canonicalizeURIPath = true;

    /**
     * Determines if URI should be normalized
     */
    boolean normalizeURI = true;
    
    /**
     * matrix params should be ignored
     */
    boolean ignoreMatrixParams = true;
    
    /**
     * output directory for apt
     */
    String destDirectory = null;
    
    PrintWriter resourcebeanWriter;
    PrintWriter servletWriter;
    
    
    /**
     * output directory for wadl and web.xml (in web-inf subdirectory)
     */
    String restDestDirectory = "";    
    
    boolean verbose = false;
    
    /**
     * Creates a new instance of UriTemplateProcessor
     */
    public UriTemplateProcessor(AnnotationProcessorContext context) {
        this.context = context;
    }
    
    public void init(AnnotationProcessorEnvironment apEnv) {
        this.apEnv = apEnv;
        context.setAnnotationProcessorEnvironment(apEnv);
        Map<String, String>options = apEnv.getOptions();
        destDirectory = options.get("-d");
        verbose = options.containsKey("-verbose");
        for (String key : options.keySet()) {
            log("key: "+ key+ " value: "+options.get(key));
            if (key.startsWith("-Aurlpattern")) {
                urlPattern = key.split("=")[1];
            } else if (key.startsWith("-Aservletclassname")) {
                servletClassName = key.split("=")[1];
            } else if (key.startsWith("-Aservletname")) {
                servletName = key.split("=")[1];
            } else if (key.startsWith("-Awebresourcespkg")) {
                webresourcesPackage = key.split("=")[1];
            } else if (key.startsWith("-Aservlet")) {
                generateWebXml = true;
            } else if (key.startsWith("-Anowadl")) {
                generateWadl = false;
            } else if (key.startsWith("-Aredirect")) {
                redirect = !"false".equalsIgnoreCase(key.split("=")[1]);
            } else if (key.startsWith("-AnormalizeURI")) {
                normalizeURI = !"false".equalsIgnoreCase(key.split("=")[1]);
            } else if (key.startsWith("-AcanonicalizeURIPath")) {
                canonicalizeURIPath = !"false".equalsIgnoreCase(key.split("=")[1]);
            } else if (key.startsWith("-AignoreMatrixParams")) {
                ignoreMatrixParams = !"false".equalsIgnoreCase(key.split("=")[1]);
            } else if (key.startsWith("-Awebresourcesdestdir")) {
                restDestDirectory = key.split("=")[1];
                if (!(restDestDirectory.endsWith("/") ||
                      restDestDirectory.endsWith("\\"))) {
                    restDestDirectory += File.separator;
                }
            }
        }
        
        context.getRCFeatures().put(ResourceConfig.FEATURE_NORMALIZE_URI, normalizeURI);
        context.getRCFeatures().put(ResourceConfig.FEATURE_REDIRECT, redirect);
        context.getRCFeatures().put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, canonicalizeURIPath);
        context.getRCFeatures().put(ResourceConfig.FEATURE_IGNORE_MATRIX_PARAMS, ignoreMatrixParams);
        
        if (!generateWebXml) {
           if (servletClassName != null)
               apEnv.getMessager().printError("-Aservletclassname requires the -Aservlet option to be set.");
           
           if (servletName != null)
               apEnv.getMessager().printError("-Aservletname requires the -Aservlet option to be set.");
              
           if (urlPattern != null)
               apEnv.getMessager().printError("-Aurlpattern requires the -Aservlet option to be set.");
        } else {
            if (context.round < 1)
            System.err.println("Warning: The -Aservet option will generate a web.xml and override any pre-existing web.xml");
            if (servletClassName == null)
                servletClassName = "com.sun.ws.rest.impl.container.servlet.ServletAdaptor";
            if (servletName == null)
                servletName = "Jersey Web Application";
            if (urlPattern == null)
                urlPattern = "/resources/*";
        }
    }
    
    public void process() {
        if (context.round > 1)
            return;
        log("round: "+context.round);
        if (processAnnotations()) {      
            if (context.round == 0 && generateWadl) {
                WadlWriter wadlWriter = new WadlWriter(context, urlPattern);
                WadlResourceGenerator wadlResourceGen = new WadlResourceGenerator(context, null);
                try {
                    wadlResourceGen.generate();
                    String pkg = wadlResourceGen.getPackage();
                    pkg = pkg.replaceAll("\\.", "\\"+File.separator);
                    String wadl = pkg + (pkg.length() > 0 ? File.separator : "");
                    wadl+="application.wadl";
                    wadlWriter.write(apEnv.getFiler().createTextFile(Filer.Location.CLASS_TREE, "",new File(wadl), null));
                } catch (IOException ex) {
                    reportError(ex.getMessage());
                    if (verbose)
                        ex.printStackTrace();
                }
            } else if (context.round == 1 || !generateWadl) {
                WebAppWriter webAppWriter = new WebAppWriter(servletClassName, servletName, urlPattern, context);
                WebResourcesGenerator classGen = new WebResourcesGenerator(this, context);
                classGen.generateResourceClassSet(webresourcesPackage);
                try {
                    if (generateWebXml) {
                        String webxml = restDestDirectory+"WEB-INF/web.xml";
                        webAppWriter.write(apEnv.getFiler().createTextFile(Filer.Location.CLASS_TREE, "",new File(webxml), null));
                    }
                } catch (IOException ex) {
                    reportError(ex.getMessage());
                    if (verbose)
                        ex.printStackTrace();
                }
            }
        }
        context.round++;
    }
    
    private boolean processAnnotations() {
        boolean processedAnnotations = false;
        for (TypeDeclaration typedecl : apEnv.getTypeDeclarations()) {
            if (!(typedecl instanceof ClassDeclaration))
                continue;
            UriTemplate uriTemplate = typedecl.getAnnotation(UriTemplate.class);
            if (uriTemplate == null)
                continue;
            Resource r = processResource(typedecl, uriTemplate);
            if (r != null)
                context.getResources().add(r);
            processedAnnotations = true;
        }
        return processedAnnotations;
    }
    
    private Resource processResource(TypeDeclaration decl, UriTemplate uriTemplate) {
        log("Processing resource: "+decl.getQualifiedName());
        Resource r = new Resource(decl.getQualifiedName(), uriTemplate.value());
        
        ConsumeMime c = decl.getAnnotation(ConsumeMime.class);
        if (c != null)
            r.setConsumes(c.value());
        
        ProduceMime p = decl.getAnnotation(ProduceMime.class);
        if (p != null)
            r.setProduces(p.value());
        
        for (MethodDeclaration md: decl.getMethods()) {
            processMethod(r, md);
        }
        
        return r;
    }
    
    private void processMethod(Resource r, MethodDeclaration md) {
        HttpMethod httpMethod = md.getAnnotation(HttpMethod.class);
        if (httpMethod != null) {
            if (!md.getModifiers().contains(Modifier.PUBLIC)) {
                System.err.printf("Warning: Ignoring non-public method %s of class %s\n",
                        md.getSimpleName(), r.getClassName());
                return;
            }
            Method m = new Method(
                    ResourceHttpMethod.getHttpMethod(httpMethod, md.getSimpleName()),
                    r);
            ConsumeMime c = md.getAnnotation(ConsumeMime.class);
            if (c != null)
                m.setConsumes(c.value());
            
            ProduceMime p = md.getAnnotation(ProduceMime.class);
            if (p != null)
                m.setProduces(p.value());
            
            if (!(md.getReturnType() instanceof VoidType))
                m.setOutputEntity(true);
            
            for (ParameterDeclaration pd: md.getParameters()) {
                Param param = processParameter(m, pd);
                m.getParams().add(param);
                if (param.getStyle() == Param.Style.ENTITY)
                    m.setInputEntity(true);
            }
        }
        return;
    }
    
    private Param processParameter(Method m, ParameterDeclaration pd) {
        TypeChecker type = new TypeChecker();
        pd.getType().accept(type);
        DefaultValue d = pd.getAnnotation(DefaultValue.class);
        String defaultValue = d == null ? null : d.value();
        QueryParam qp = pd.getAnnotation(QueryParam.class);
        if (qp != null)
            return new Param(qp.value(), Param.Style.QUERY, defaultValue, type.getSchemaType(), type.isRepeating());
        UriParam up = pd.getAnnotation(UriParam.class);
        if (up != null)
            return new Param(up.value(), Param.Style.URI, defaultValue, type.getSchemaType(), type.isRepeating());
        return new Param(pd.getSimpleName(), Param.Style.ENTITY, defaultValue, type.getSchemaType(), type.isRepeating());
    }
    
    public void log(String msg) {
        if (verbose)
            apEnv.getMessager().printNotice("[UriTemplateProcessor] "+msg); 
    }

    public void reportError(String msg) {
        apEnv.getMessager().printError("[UriTemplateProcessor] "+msg); 
    }
    
    public class TypeChecker extends SimpleTypeVisitor {
        
        protected QName schemaType;
        protected boolean repeating;
        
        public TypeChecker() {
            schemaType = new QName("http://www.w3.org/2001/XMLSchema", "string", "xs");
            repeating = false;
        }

        public QName getSchemaType() {
            return schemaType;
        }

        public boolean isRepeating() {
            return repeating;
        }
        
        public void visitPrimitiveType(PrimitiveType primitiveType) {
            switch (primitiveType.getKind()) {
                case BOOLEAN:
                    schemaType = new QName("http://www.w3.org/2001/XMLSchema", "boolean", "xs");
                    break;
                case INT:
                    schemaType = new QName("http://www.w3.org/2001/XMLSchema", "int", "xs");
                    break;
                case LONG:
                    schemaType = new QName("http://www.w3.org/2001/XMLSchema", "long", "xs");
                    break;
                case SHORT:
                    schemaType = new QName("http://www.w3.org/2001/XMLSchema", "short", "xs");
                    break;
                case BYTE:
                    schemaType = new QName("http://www.w3.org/2001/XMLSchema", "byte", "xs");
                    break;
                case FLOAT:
                    schemaType = new QName("http://www.w3.org/2001/XMLSchema", "float", "xs");
                    break;
                case DOUBLE:
                    schemaType = new QName("http://www.w3.org/2001/XMLSchema", "double", "xs");
                    break;
            }
        }

        public void visitEnumType(EnumType enumType) {
            // TODO implement support for Enums
        }

        public void visitArrayType(ArrayType arrayType) {
            repeating = true;
            arrayType.getComponentType().accept(this);
        }

    }
}

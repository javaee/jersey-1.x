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
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.util.SimpleTypeVisitor;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.model.method.ResourceHttpMethod;
import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import com.sun.ws.rest.tools.annotation.Method;
import com.sun.ws.rest.tools.annotation.Param;
import com.sun.ws.rest.tools.annotation.Resource;
import com.sun.ws.rest.tools.wadl.resource.WadlResourceGenerator;
import com.sun.ws.rest.tools.wadl.writer.WadlWriter;
import com.sun.ws.rest.tools.Messager;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.UriParam;
import javax.ws.rs.Path;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.xml.namespace.QName;
 

/**
 *
 * @author Doug Kohlert
 */

public class UriTemplateProcessor implements Messager, AnnotationProcessor {
    
    /**
     * HTTP methods that may be used as the prefix of a Java method name.
     * TODO: get rid of it (used by getHttpMethod helper method)
     */
    private enum COMMON_METHODS {GET, POST, PUT, DELETE, HEAD};

    private AnnotationProcessorEnvironment apEnv;
    
    private AnnotationProcessorContext context;
    
    /**
     * The Base URL used for the WebApplication
     * This will be used to generated the &lt;url-pattern> in the web.xml
     */
    private String urlPattern = null;
                        
    /**
     * output directory for apt
     */
    private String destDirectory = null;
        
    /**
     * output directory for wadl and web.xml (in web-inf subdirectory)
     */
    private String restDestDirectory = "";    
    
    private boolean verbose = false;
    
    /**
     * Creates a new instance of UriTemplateProcessor
     */
    public UriTemplateProcessor(AnnotationProcessorContext context) {
        this.context = context;
    }
    
    public void init(AnnotationProcessorEnvironment apEnv) {
        this.apEnv = apEnv;
        context.setAnnotationProcessorEnvironment(apEnv);
        
        // TODO can we use args4J to improve this code?
        Map<String, String>options = apEnv.getOptions();
        destDirectory = options.get("-d");
        verbose = options.containsKey("-verbose");
        context.setVerbose(verbose);
        for (String key : options.keySet()) {
            log("key: "+ key+ " value: "+options.get(key));
            if (key.startsWith("-Aurlpattern")) {
                urlPattern = getStringValue(key);
            } else if (key.startsWith("-Awebresourcesdestdir")) {
                restDestDirectory = key.split("=")[1];
                if (!(restDestDirectory.endsWith("/") ||
                      restDestDirectory.endsWith("\\"))) {
                    restDestDirectory += File.separator;
                }
            }
        }                
    }
    
    private String getStringValue(String expression) {
        return getStringValue(expression, true);
    }
    
    private String getStringValue(String expression, boolean trim) {
        String[] splitExpression = expression.split("=");
        if (splitExpression.length == 2) {
            String value = (trim) ? splitExpression[1].trim() : splitExpression[1];
            return (value.length() == 0) ? null : value;
        } else {
            return null;        
        }
    }
    
    public void process() {
        if (context.round > 0)
            return;
        log("round: "+context.round);
        if (processAnnotations()) {      
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
        }
        context.round++;
    }
    
    private boolean processAnnotations() {
        boolean processedAnnotations = false;
        for (TypeDeclaration typedecl : apEnv.getTypeDeclarations()) {
            if (!(typedecl instanceof ClassDeclaration))
                continue;
            Path path = typedecl.getAnnotation(Path.class);
            if (path == null)
                continue;
            Resource r = processResource(typedecl, path);
            if (r != null)
                context.getResources().add(r);
            processedAnnotations = true;
        }
        return processedAnnotations;
    }
    
    private Resource processResource(TypeDeclaration decl, Path path) {
        log("Processing resource: "+decl.getQualifiedName());
        Resource r = new Resource(decl.getQualifiedName(), path.value());
        
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
                    getHttpMethod(httpMethod, md.getSimpleName()),
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
        MatrixParam mp = pd.getAnnotation(MatrixParam.class);
        if (mp != null)
            return new Param(mp.value(), Param.Style.MATRIX, defaultValue, type.getSchemaType(), type.isRepeating());
        HeaderParam hp = pd.getAnnotation(HeaderParam.class);
        if (hp != null)
            return new Param(hp.value(), Param.Style.HEADER, defaultValue, type.getSchemaType(), type.isRepeating());
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
    
    private static String getHttpMethod(HttpMethod httpMethod, String javaMethodName) {
        if (httpMethod.value().length() > 0)
            return httpMethod.value();
        
        String methodName = javaMethodName.toUpperCase();
        for (COMMON_METHODS methodConstant : COMMON_METHODS.values()) {
            if (methodName.startsWith(methodConstant.toString())) {
                return methodConstant.toString();
            }
        }

        return "";
    }    
    
}

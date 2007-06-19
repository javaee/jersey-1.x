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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.ws.rest.tools.annotation.AnnotationProcessorContext;
import java.util.HashMap;
import java.util.Map;


/*
 * The {@com.sun.mirror.apt.AnnotationProcessorFactory AnnotationProcessorFactory}
 * class used by the <a href="http://java.sun.com/j2se/1.5.0/docs/tooldocs/share/apt.html">APT</a>
 * framework.
 *
 * @author Doug Kohlert
 */
public class AnnotationProcessorFactoryImpl implements AnnotationProcessorFactory {
    
    /*
     * Options supported by this processor.
     */
    static Collection<String> supportedOptions;
    
    static  UriTemplateProcessor utAP = null;
    
    /*
     * Supports com.sun.ws.rest.* annotations.
     */
    static Collection<String> supportedAnnotations;
    static {
        Collection<String> types = new HashSet<String>();
        types.add("javax.ws.rs.UriTemplate");
        types.add("javax.ws.rs.HttpMethod");
        types.add("javax.ws.rs.ProduceMime");
        types.add("javax.ws.rs.ConsumeMime");
        types.add("javax.ws.rs.UriParam");
        types.add("javax.ws.rs.QueryParam");
        supportedAnnotations = Collections.unmodifiableCollection(types);
        
        // options
        Set<String> options = new HashSet<String>();
        // The servlet-class-name used in the generated web.xml 
        options.add("-Aservletclassname");
        // The servlet-name used in the generated web.xml 
        options.add("-Aservletname");
        // The url-pattern
        options.add("-Aurlpattern");
        // The package that the generated RESTResources class will be generated
        options.add("-Awebresourcespkg");
        supportedOptions = Collections.unmodifiableSet(options);
        
    }
    
    public AnnotationProcessorFactoryImpl() {
    }
    
    
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }
    
    public Collection<String> supportedAnnotationTypes() {
        return supportedAnnotations;
    }
    
    /*
     * Return an instance of the {@link UriTemplateProcessor} AnnotationProcesor.
     */
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
            AnnotationProcessorEnvironment apEnv) {
        if (utAP == null) {
            AnnotationProcessorContext context = new AnnotationProcessorContext();
            utAP = new UriTemplateProcessor(context);        
        }
        utAP.init(apEnv);
        return utAP;
    }
}





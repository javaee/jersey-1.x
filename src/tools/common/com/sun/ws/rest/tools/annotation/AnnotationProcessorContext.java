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

package com.sun.ws.rest.tools.annotation;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import java.io.File;
import java.util.ArrayList;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author  Doug Kohlert
 */
public class AnnotationProcessorContext {
    private Collection<Resource> resources;
    
    /** value of -d option or "." */
    private File classDir;
    
    /** value of -s option or <code>classDir</code> */
    private File sourceDir;
    
    /** the APT filer used to create new files */
    private Filer filer;
    
    /** the <code>AnnotationProcessorEnvironment</code> associated with
     * this <code>AnnotationProcessorContext</code>
     */
    private AnnotationProcessorEnvironment apEnv;
    
    /** The name of the <code>ResourceBean</code> to be generated. */
    private String resourceBeanClassName;
    
    /** Features to go to ResourceConfig */
    private final Map<String, Boolean> features = new HashMap<String, Boolean>();
    
    /** 
     * APT round number 
     */
    public int round = 0;
    
    /** Creates a new instance of AnnotationProcessorContext */
    public AnnotationProcessorContext() {
        resources = new ArrayList<Resource>();    
    }
    
    public void setAnnotationProcessorEnvironment(AnnotationProcessorEnvironment apEnv) {
        if (apEnv == null)
            return;
        this.apEnv = apEnv;
        Map<String, String>options = apEnv.getOptions();
        String tmp = options.get("-d");
        if (tmp == null)
            classDir = new File(".");
        else
            classDir = new File(tmp);
        sourceDir = classDir;
        if (options.get("-s") != null)
            sourceDir = new File(options.get("-s"));
        filer = apEnv.getFiler();
    }
    
    public Collection<String> getResourceClasses() {
        ArrayList<String> list = new ArrayList<String>();
        for (Resource r: resources) {
            list.add(r.getClassName());
        }
        return list;
    }
    
    public Collection<Resource> getResources() {
        return resources;
    }
   
    public File getClassDir() {
        return classDir;
    }
    
    public File getSourceDir() {
        return sourceDir;
    }
    
    public Filer getFiler() {
        return filer;
    }

    public AnnotationProcessorEnvironment getAPEnv() {
        return apEnv;
    }
    
    public void setResourceBeanClassName(String name) {
        if (resourceBeanClassName == null)
            resourceBeanClassName = name;
    }
    
    public String getResourceBeanClassName() {
        return resourceBeanClassName;
    }
    
    public Map<String, Boolean> getRCFeatures() {
        return features;
    }
}

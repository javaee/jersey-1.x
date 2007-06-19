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

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.mirror.util.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Doug Kohlert
 */
public class TestAnnotationProcessorEnvironment implements AnnotationProcessorEnvironment {
    private Filer filer;
    
    /** Creates a new instance of TestAnnotationProcessorEnvironment */
    public TestAnnotationProcessorEnvironment(Filer filer) {
        this.filer = filer;
    }
 
    public void addListener(AnnotationProcessorListener listener) {        
    }
    
    public Collection<Declaration> getDeclarationsAnnotatedWith(AnnotationTypeDeclaration a) {
        return null;
    }
    
    public Declarations getDeclarationUtils() {
        return null;
    }
    
    public Filer getFiler() {
        return filer;
    }
    
    public Messager getMessager() {
        return null;
    }
    
    public Map<String, String> getOptions() {
        Map<String, String>map = new HashMap<String, String>();
        map.put("-d", ".");
        map.put("-s", ".");
        return map;
    }
    
    public PackageDeclaration getPackage(String name) {
        return null;
    }
    
    public Collection<TypeDeclaration> getSpecifiedTypeDeclarations() {
        return null;
    }
    
    public TypeDeclaration getTypeDeclaration(String name) {
        return null;
    }
    
    public Collection<TypeDeclaration> getTypeDeclarations() {
        return null;
    }
    
    public Types getTypeUtils() {
        return null;
    }
    
    public void removeListener(AnnotationProcessorListener listener) {
    }
}

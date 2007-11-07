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

package com.sun.ws.rest.impl.container.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Search for annotated Root resource classes.
 *
 * @author Frank D. Martinez. fmartinez@asimovt.com
 */
public class ResourceClassScanner {
    
    /** Resource classes found. */
    private List<Class> classes;
    
    /** Annotation class names we are looking for. */
    private Set<String> annotations;
    
    /**
     * Scans into 'paths' for classes annotated with 'annotations'.
     * @param paths A list of absolute paths used to scan for Resource classes.
     * @param annotations A list of annotation classes to be scanned for.
     * @return A list of classes annotated with specified annotations in the specified paths.
     */
    public List<Class> scan(File[] paths, Class... annotations) {
        try {
            
            // 1. Init classes
            this.classes = new LinkedList<Class>();
            
            // 2. Init annotations
            this.annotations = new HashSet<String>();
            for (Class cls : annotations) {
                this.annotations.add(
                    "L" + cls.getName().replaceAll("\\.", "/") + ";");
            }
            
            // 3. Search
            for (File file : paths) {
                index(file);
            }
            
            // 4. Return
            return classes;
            
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private void analyze(URL file)
    throws IOException, ClassNotFoundException {
        
        ClassReader reader = new ClassReader(file.openStream());
        CLASS_VISITOR.reset();
        reader.accept(CLASS_VISITOR, 0);
        if (CLASS_VISITOR.isRootResourceClass()) {
            classes.add(Class.forName(
                reader.getClassName().replaceAll("/", ".")));
        }
        
    }
    
    private void index(File file)
    throws MalformedURLException, IOException, ClassNotFoundException {
        
        if (file.isDirectory()) {
            indexDir(file);
        } else {
            indexJar(file);
        }
        
    }
    
    private void indexDir(File root)
    throws MalformedURLException, IOException, ClassNotFoundException {
        
        File[] children = root.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                indexDir(child);
            } else if (child.getName().endsWith(".jar")) {
                indexJar(child);
            } else if (child.getName().endsWith(".class")) {
                // File.toURL() is deprecated in SE 6
                analyze(child.toURI().toURL());
            }
        }
        
    }
    
    private void indexJar(File file)
    throws IOException, ClassNotFoundException {
        
        final JarFile jar = new JarFile(file);
        final Enumeration<JarEntry> entries = jar.entries();
        final String jarBase = "jar:" + file.toURI() + "!/";
        while (entries.hasMoreElements()) {
            JarEntry e = entries.nextElement();
            if (e.getName().endsWith(".class")) {
                analyze(new URL(jarBase + e.getName()));
            }
        }
        
    }
    
    private final ResourceClassVisitor CLASS_VISITOR = new ResourceClassVisitor();
    
    private final class ResourceClassVisitor implements ClassVisitor {
        
        boolean isAnnotated = false;
        boolean isPublic = false;
        boolean isInnerClass = false;
        boolean isStatic = false;
        
        public void reset() {
            isAnnotated = isPublic = isInnerClass = isStatic = false;
        }
        
        public boolean isRootResourceClass() {
            return isAnnotated && isPublic && (!isInnerClass || isStatic);
        }
        
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            isAnnotated |= annotations.contains(desc);
            return null;
        }
        
        public void visit(int version, int access, String name, String
            signature, String superName, String[] interfaces) {
            isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
            isInnerClass = name.indexOf("$") > -1;
        }
        
        public void visitSource(String string, String string0) {
            // Do nothing
        }
        
        public void visitOuterClass(String string, String string0,
            String string1) {
            // Do nothing
        }
        
        public void visitEnd() {
        }
        
        public FieldVisitor visitField(int i, String string,
            String string0, String string1, Object object) {
            // Do nothing
            return null;
        }
        
        public void visitAttribute(Attribute attribute) {
            // Do nothing
        }
        
        public MethodVisitor visitMethod(int i, String string,
            String string0, String string1, String[] string2) {
            // Do nothing
            return null;
        }
        
        public void visitInnerClass(String name, String outerName,
            String innerName, int access) {
            if (isInnerClass) {
                isPublic = (access & Opcodes.ACC_PUBLIC) != 0;
                isStatic = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
            }
        }
        
    };
}

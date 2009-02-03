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

package com.sun.jersey.server.impl.container.config;

import com.sun.jersey.core.reflection.ReflectionHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Search for Java classes that are annotated with one or more of a set
 * of annotations.
 * <p>
 * The search is restricted to Java classes that are publically scoped.
 * Inner static public classes are also searched.
 * 
 * @author Frank D. Martinez. fmartinez@asimovt.com
 */
public class AnnotatedClassScanner {
    private static final Logger LOGGER = 
            Logger.getLogger(AnnotatedClassScanner.class.getName());
    
    /** Matching annotated classes. */
    private Set<Class<?>> classes;
    
    /** Set of annotations to search for. */
    private final Set<String> annotations;
    
    /** The class loader to use to load matching Java class files */
    private final ClassLoader classloader;

    
    /**
     * 
     * @param annotations the set of annotations to match
     */
    public AnnotatedClassScanner(Class... annotations) {
        this.classloader = ReflectionHelper.getContextClassLoader();
        this.annotations = getAnnotationSet(annotations);
        this.classes = new HashSet<Class<?>>();
    }
    
    /**
     * Scans paths for matching Java classes
     * 
     * @param paths An array of absolute paths to search.
     * @return The set of matching classes that are annotated with one or more of
     *         the specified annotations.
     */
    public Set<Class<?>> scan(File[] paths) {
        this.classes = new HashSet<Class<?>>();
        
        for (File file : paths) {
            index(file);
        }

        return classes;
    }

    /**
     * Scans packages for matching Java classes.
     * 
     * @param packages An array of packages to search.
     * @return The set of matching classes that are annotated with one or more of
     *         the specified annotations.
     */
    public Set<Class<?>> scan(String[] packages) {
        this.classes = new HashSet<Class<?>>();
        
        for (String p : packages) {
            try {
                String fileP = p.replace('.', '/');
                Enumeration<URL> urls = classloader.getResources(fileP);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try {
                        URI uri = getURI(url);
                        index(uri, fileP);
                    } catch (URISyntaxException e) {
                        LOGGER.warning("URL, " + 
                                url + 
                                "cannot be converted to a URI");                        
                    }
                }
            } catch (IOException ex) {
                String s = "The resources for the package" + 
                        p + 
                        ", could not be obtained";
                LOGGER.severe(s);
                throw new RuntimeException(s, ex);
            }
        }
        
        return classes;
    }

    private URI getURI(URL url) throws URISyntaxException {
        if (url.getProtocol().equalsIgnoreCase("vfsfile")) {
            // Used with JBoss 5.x: trim prefix "vfs"
            return new URI(url.toString().substring(3));
        } else {
            return url.toURI();
        }
    }

    /**
     * Get the current set of matching classes.
     * 
     * @return The set of matching classes that are annotated with one or more of
     *         the specified annotations.
     */
    public Set<Class<?>> getMatchingClasses() {
        return classes;
    }
    
    private Set<String> getAnnotationSet(Class... annotations) {
        Set<String> a = new HashSet<String>();
        for (Class cls : annotations) {
            a.add(
                "L" + cls.getName().replaceAll("\\.", "/") + ";");
        }
        return a;
    }
    
    private void index(File file) {
        if (file.isDirectory()) {
            indexDir(file, true);
        } else if (file.getName().endsWith(".jar") || 
                file.getName().endsWith(".zip")) {
            indexJar(file);
        } else {
            LOGGER.warning("File, " + 
                    file.getAbsolutePath() + 
                    ", is ignored, it not a directory, a jar file or a zip file");
        }
    }
    
    private void index(URI u, String filePackageName) {
        String scheme = u.getScheme();
        if (scheme.equals("file")) {
            File f = new File(u.getPath());
            if (f.isDirectory()) {
                indexDir(f, false);
            } else {
                LOGGER.warning("URL, " + 
                        u + 
                        ", is ignored. The path, " + 
                        f.getPath() +
                        ", is not a directory");                
            }
        } else if (scheme.equals("jar") || scheme.equals("zip")) {
            URI jarUri = URI.create(u.getRawSchemeSpecificPart());
            String jarFile = jarUri.getPath();
            jarFile = jarFile.substring(0, jarFile.indexOf('!'));            
            indexJar(new File(jarFile), filePackageName);
        } else {
            LOGGER.warning("URL, " + 
                    u + 
                    ", is ignored, it not a file or a jar file URL");            
        }
    }
    
    private void indexDir(File root, boolean indexJars) {
        for (File child : root.listFiles()) {
            if (child.isDirectory()) {
                indexDir(child, indexJars);
            } else if (indexJars && child.getName().endsWith(".jar")) {
                indexJar(child);
            } else if (child.getName().endsWith(".class")) {
                analyzeClassFile(child.toURI());
            }
        }        
    }
    
    private void indexJar(File file) {
        indexJar(file, "");
    }

    private void indexJar(File file, String parent) {
        final JarFile jar = getJarFile(file);
        try {
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                if (!e.isDirectory() && e.getName().startsWith(parent) &&
                        e.getName().endsWith(".class")) {
                    analyzeClassFile(jar, e);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception while processing file, " + file, e);
        } finally {
            try {
                if (jar != null) {
                    jar.close();
                }
            } catch (IOException ex) {
                String s = "Error closing jar file, " +
                        jar.getName();
                LOGGER.severe(s);
            }
        }
    }
    
    private JarFile getJarFile(File file) {
        if (file == null) {
            return null;
        }
        try {
            return new JarFile(file);
        } catch (IOException ex) {
            String s = "File, " + 
                    file.getAbsolutePath() +
                    ", is not a jar file";
            LOGGER.severe(s);
            throw new RuntimeException(s, ex);
        }
    }
    
    private void analyzeClassFile(URI classFileUri) {        
        getClassReader(classFileUri).accept(classVisitor, 0);
    }
    
    private void analyzeClassFile(JarFile jarFile, JarEntry entry) {        
        getClassReader(jarFile, entry).accept(classVisitor, 0);
    }
    
    
    private ClassReader getClassReader(JarFile jarFile, JarEntry entry) {
        InputStream is = null;
        try {
            is = jarFile.getInputStream(entry);
            ClassReader cr = new ClassReader(is);
            return cr;
        } catch (IOException ex) {
            String s = "Error accessing input stream of the jar file, " + 
                    jarFile.getName() + ", entry, " + entry.getName();
            LOGGER.severe(s);
            throw new RuntimeException(s, ex);
        } finally {
            try {
                if (is != null) {
                   is.close();
                }
            } catch (IOException ex) {
                String s = "Error closing input stream of the jar file, " + 
                    jarFile.getName() + ", entry, " + entry.getName() + ", closed.";
                LOGGER.severe(s);
            }
        }
    }

    
    private ClassReader getClassReader(URI classFileUri) {
        InputStream is = null;
        try {
            is = classFileUri.toURL().openStream();
            ClassReader cr = new ClassReader(is);
            return cr;
        } catch (IOException ex) {
            String s = "Error accessing input stream of the class file URI, " + 
                    classFileUri;
            LOGGER.severe(s);
            throw new RuntimeException(s, ex);
        } finally {
            try {
                if (is != null) {
                   is.close();
                }
            } catch (IOException ex) {
            String s = "Error closing input stream of the class file URI, " + 
                    classFileUri;
            LOGGER.severe(s);
            }
        }
    }
    
    private Class getClassForName(String className) {
        try {
            return ReflectionHelper.classForNameWithException(className, classloader);
        } catch (ClassNotFoundException ex) {
            String s = "A class file of the class name, " + 
                    className + 
                    "is identified but the class could not be found";
            LOGGER.severe(s);
            throw new RuntimeException(s, ex);
        }
    }
    
    private final AnnotatedClassVisitor classVisitor = new AnnotatedClassVisitor();
    
    private final class AnnotatedClassVisitor implements ClassVisitor {
        /**
         * The name of the visited class.
         */
        private String className;
        
        /**
         * True if the class has the correct scope
         */
        private boolean isScoped;        
        
        /**
         * True if the class has the correct declared annotations
         */
        private boolean isAnnotated;
        
        public void visit(int version, int access, String name, 
                String signature, String superName, String[] interfaces) {
            className = name;
            isScoped = (access & Opcodes.ACC_PUBLIC) != 0;
            isAnnotated = false;
        }
        
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            isAnnotated |= annotations.contains(desc);
            return null;
        }
        
        public void visitInnerClass(String name, String outerName, 
                String innerName, int access) {
            // If the name of the class that was visited is equal
            // to the name of this visited inner class then
            // this access field needs to be used for checking the scope
            // of the inner class
            if (className.equals(name)) {
                isScoped = (access & Opcodes.ACC_PUBLIC) != 0;
                
                // Inner classes need to be statically scoped
                isScoped &= (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
            }
        }
        
        public void visitEnd() {
            if (isScoped && isAnnotated) {
                // Correctly scoped and annotated
                // add to the set of matching classes.
                classes.add(getClassForName(className.replaceAll("/", ".")));    
            }
        }
        
        
        public void visitOuterClass(String string, String string0, 
                String string1) {
            // Do nothing
        }
        
        public FieldVisitor visitField(int i, String string, 
                String string0, String string1, Object object) {
            // Do nothing
            return null;
        }
        
        public void visitSource(String string, String string0) {
            // Do nothing
        }
        
        public void visitAttribute(Attribute attribute) {
            // Do nothing
        }
        
        public MethodVisitor visitMethod(int i, String string, 
                String string0, String string1, String[] string2) {
            // Do nothing
            return null;
        }        
    };
}

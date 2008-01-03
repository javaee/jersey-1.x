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
package com.sun.ws.rest.api.wadl;

import com.sun.research.ws.wadl.Application;
import com.sun.ws.rest.api.core.DynamicResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.api.model.AbstractResource;
import com.sun.ws.rest.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.ws.rest.impl.wadl.WadlGenerator;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

/**
 * @author Paul.Sandoz@Sun.Com
 */
public class WadlGeneratorTask extends Task {

    private Path classpath;

    public Path getClasspath() {
        return classpath;
    }

    public void setClasspath(Path classpath) {
        if (classpath != null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    
    
    private File wadlFile;

    public File getDestfile() {
        return wadlFile;
    }

    public void setDestfile(File wadlFile) {
        this.wadlFile = wadlFile;
    }
    
    
    @Override
    public void execute() throws BuildException {
        if (classpath == null) {
            throw new BuildException("The classpath is not defined");
        }
        if (wadlFile == null) {
            throw new BuildException("destfile attribute required", getLocation());
        }

        try {
            Application a = createApplication(classpath.list());
            JAXBContext c = JAXBContext.newInstance("com.sun.research.ws.wadl", 
                    this.getClass().getClassLoader());
            Marshaller m = c.createMarshaller();
            m.marshal(a, wadlFile);
        } catch (Exception e) {
            throw new BuildException(e);            
        }
    }

    private Application createApplication(String[] paths) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final ClassLoader ncl = new Loader(classpath.list(), this.getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(ncl);
        try {
            ResourceConfig rc = new DynamicResourceConfig(classpath.list());
            Set<AbstractResource> s = new HashSet<AbstractResource>();
            for (Class c : rc.getResourceClasses()) {
                s.add(IntrospectionModeller.createResource(c));
            }
            return WadlGenerator.generate(s);
        } catch(Exception e) {
            throw new BuildException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }        
    }
    
    private static class Loader extends URLClassLoader {
        Loader(String[] paths, ClassLoader parent) {
            super(getURLs(paths), parent);
        }
        
        Loader(String[] paths) {
            super(getURLs(paths));
        }
        
        @Override
        public Class findClass(String name) throws ClassNotFoundException {
            Class c = super.findClass(name);
            return c;
        }
        
        private static URL[] getURLs(String[] paths) {
            List<URL> urls = new ArrayList<URL>();
            for (String path: paths) {
                try {
                    urls.add(new File(path).toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
            
            URL[] us = urls.toArray(new URL[0]);
            return us;
        }
    }
}
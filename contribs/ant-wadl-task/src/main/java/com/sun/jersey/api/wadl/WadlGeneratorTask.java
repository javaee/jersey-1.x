/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package com.sun.jersey.api.wadl;

import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;
import com.sun.jersey.server.wadl.ApplicationDescription;
import com.sun.jersey.server.wadl.WadlBuilder;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Resources;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ant task for generating WADL for root resource classes.
 * 
 * The task requires that the destfile attribute be set to the location
 * of the WADL file to be generated, the baseUri attribute set to the base
 * URI of the WADL resources, and the classpath be set.
 * 
 * The task will scan all classes in the classpath obtain the root resource
 * classes and then create a WADL document from those root resources.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class WadlGeneratorTask extends Task {

    private Path classpath;

    public Path getClasspath() {
        return classpath;
    }

    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
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

    private String baseUri;
    
    public String getbaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }
    
    @Override
    public void execute() throws BuildException {
        if (classpath == null) {
            throw new BuildException("The classpath is not defined");
        }
        if (wadlFile == null) {
            throw new BuildException("destfile attribute required", getLocation());
        }

        if (baseUri == null || baseUri.length() == 0) {
            throw new BuildException("baseUri attribute required", getLocation());
        }
        
        try {
            ApplicationDescription ad = createApplication(classpath.list());
            
            WRITE_OUT_WADL : {
                Application a = ad.getApplication();
                for(Resources resources : a.getResources())
                        resources.setBase(baseUri);
                JAXBContext c = JAXBContext.newInstance("com.sun.research.ws.wadl", 
                        this.getClass().getClassLoader());
                Marshaller m = c.createMarshaller();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(wadlFile));
                try {
                    m.marshal(a, out);
                }
                finally {
                    out.close();
                }
            }
            
            WRITE_OUT_EXTERNAL_DATA : {
                // TODO work out how to reconsile the different paths
                File wadlChildren = new File(wadlFile.getPath() + "-/");
                wadlChildren.mkdirs();
                for (String key : ad.getExternalMetadataKeys()) {
                    
                    // Create the next file based on the key
                    //
                    File nextFile = new File(wadlChildren, "key");
                    ApplicationDescription.ExternalGrammar em = ad.getExternalGrammar( key );

                    // Write a copy to disk
                    //
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(nextFile));
                    try {
                        out.write( em.getContent() );
                    }
                    finally {
                        out.close();
                    }
                }
            }
            
        } catch (Exception e) {
            throw new BuildException(e);            
        }
    }

    private ApplicationDescription createApplication(String[] paths) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final ClassLoader ncl = new Loader(classpath.list(), this.getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(ncl);
        try {
            ResourceConfig rc = new ClasspathResourceConfig(classpath.list());
            rc.validate();
            Set<AbstractResource> s = new HashSet<AbstractResource>();
            for (Class c : rc.getRootResourceClasses()) {
                s.add(IntrospectionModeller.createResource(c));
            }
            
            return new WadlBuilder().generate(null, s);
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
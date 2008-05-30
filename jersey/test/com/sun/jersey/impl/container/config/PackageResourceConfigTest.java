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

package com.sun.jersey.impl.container.config;

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.container.config.innerstatic.InnerStaticClass;
import com.sun.jersey.impl.container.config.toplevel.PublicRootResourceClass;
import com.sun.jersey.impl.container.config.toplevelinnerstatic.PublicRootResourceInnerStaticClass;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class PackageResourceConfigTest extends AbstractResourceConfigTester {
    
    public PackageResourceConfigTest(String testName) {
        super(testName);
    }
    
    public void testTopLevel() {
        String[] packages = {"com.sun.jersey.impl.container.config.toplevel"};
        ResourceConfig rc = new PackagesResourceConfig(packages);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testInnerStatic() {
        String[] packages = {"com.sun.jersey.impl.container.config.innerstatic"};
        ResourceConfig rc = new PackagesResourceConfig(packages);
        
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testTopLevelInnerStatic() {
        String[] packages = {"com.sun.jersey.impl.container.config.toplevelinnerstatic"};
        ResourceConfig rc = new PackagesResourceConfig(packages);
                
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    public void testAll() {
        String[] packages = {"com.sun.jersey.impl.container.config"};
        ResourceConfig rc = new PackagesResourceConfig(packages);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(4, rc.getResourceClasses().size());
    }
    
    
    public void testJarTopLevel() throws Exception {
        ClassLoader cl = createClassLoader("build/test/classes/",
                "com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class"
                );
        
        ResourceConfig rc = createConfig(cl, 
                "com.sun.jersey.impl.container.config.toplevel");

        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.jersey.impl.container.config.toplevel.PublicRootResourceClass")));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testJarInnerStatic() throws Exception {
        ClassLoader cl = createClassLoader("build/test/classes/",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(cl, 
                "com.sun.jersey.impl.container.config.innerstatic");

        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.jersey.impl.container.config.innerstatic.InnerStaticClass$PublicClass")));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testJarBoth() throws Exception {
        ClassLoader cl = createClassLoader("build/test/classes/",
                "com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(cl, 
                "com.sun.jersey.impl.container.config");

        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.jersey.impl.container.config.toplevel.PublicRootResourceClass")));
        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.jersey.impl.container.config.innerstatic.InnerStaticClass$PublicClass")));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    public void testJarAsZipBoth() throws Exception {
        ClassLoader cl = createClassLoader(Suffix.zip, "build/test/classes/",
                "com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(cl, 
                "com.sun.jersey.impl.container.config");

        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.jersey.impl.container.config.toplevel.PublicRootResourceClass")));
        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.jersey.impl.container.config.innerstatic.InnerStaticClass$PublicClass")));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    private ResourceConfig createConfig(ClassLoader cl, String... packages) throws IOException {        
        ClassLoader ocl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        try {
            Class prc = cl.loadClass("com.sun.jersey.api.core.PackagesResourceConfig");
            Constructor c = prc.getConstructor(String[].class);
            return (ResourceConfig)c.newInstance((Object)packages);
            // return new PackagesResourceConfig(packages);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(ocl);
        }
    }
    
    private ClassLoader createClassLoader(String base, String... entries) throws IOException {
        return createClassLoader(Suffix.jar, base, entries);
    }
    
    private ClassLoader createClassLoader(Suffix s, String base, String... entries) throws IOException {
        URL[] us = new URL[1];
        us[0] = createJarFile(s, base, entries).toURI().toURL();
        return new PackageClassLoader(us);
    } 
    
    private static class PackageClassLoader extends URLClassLoader {
        PackageClassLoader(URL[] urls) {
            super(urls, null);
        }
        
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                return getSystemClassLoader().loadClass(name);
            }
        }        
    }
}

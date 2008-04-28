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

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.ws.rest.impl.container.config.innerstatic.InnerStaticClass;
import com.sun.ws.rest.impl.container.config.toplevel.PublicRootResourceClass;
import com.sun.ws.rest.impl.container.config.toplevelinnerstatic.PublicRootResourceInnerStaticClass;
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
        String[] packages = {"com.sun.ws.rest.impl.container.config.toplevel"};
        ResourceConfig rc = new PackagesResourceConfig(packages);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testInnerStatic() {
        String[] packages = {"com.sun.ws.rest.impl.container.config.innerstatic"};
        ResourceConfig rc = new PackagesResourceConfig(packages);
        
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testTopLevelInnerStatic() {
        String[] packages = {"com.sun.ws.rest.impl.container.config.toplevelinnerstatic"};
        ResourceConfig rc = new PackagesResourceConfig(packages);
                
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    public void testAll() {
        String[] packages = {"com.sun.ws.rest.impl.container.config"};
        ResourceConfig rc = new PackagesResourceConfig(packages);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(4, rc.getResourceClasses().size());
    }
    
    
    public void testJarTopLevel() throws Exception {
        ClassLoader cl = createClassLoader("build/test/classes/",
                "com/sun/ws/rest/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/ws/rest/impl/container/config/toplevel/PackageRootResourceClass.class"
                );
        
        ResourceConfig rc = createConfig(cl, 
                "com.sun.ws.rest.impl.container.config.toplevel");

        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.ws.rest.impl.container.config.toplevel.PublicRootResourceClass")));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testJarInnerStatic() throws Exception {
        ClassLoader cl = createClassLoader("build/test/classes/",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(cl, 
                "com.sun.ws.rest.impl.container.config.innerstatic");

        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.ws.rest.impl.container.config.innerstatic.InnerStaticClass$PublicClass")));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testJarBoth() throws Exception {
        ClassLoader cl = createClassLoader("build/test/classes/",
                "com/sun/ws/rest/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/ws/rest/impl/container/config/toplevel/PackageRootResourceClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(cl, 
                "com.sun.ws.rest.impl.container.config");

        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.ws.rest.impl.container.config.toplevel.PublicRootResourceClass")));
        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.ws.rest.impl.container.config.innerstatic.InnerStaticClass$PublicClass")));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    public void testJarAsZipBoth() throws Exception {
        ClassLoader cl = createClassLoader(Suffix.zip, "build/test/classes/",
                "com/sun/ws/rest/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/ws/rest/impl/container/config/toplevel/PackageRootResourceClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(cl, 
                "com.sun.ws.rest.impl.container.config");

        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.ws.rest.impl.container.config.toplevel.PublicRootResourceClass")));
        assertTrue(rc.getResourceClasses().contains(
                cl.loadClass("com.sun.ws.rest.impl.container.config.innerstatic.InnerStaticClass$PublicClass")));
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

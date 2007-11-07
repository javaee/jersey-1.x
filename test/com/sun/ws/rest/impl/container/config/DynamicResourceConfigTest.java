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

import com.sun.ws.rest.api.core.DynamicResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.container.config.innerstatic.InnerStaticClass;
import com.sun.ws.rest.impl.container.config.toplevel.PublicRootResourceClass;
import com.sun.ws.rest.impl.container.config.toplevelinnerstatic.PublicRootResourceInnerStaticClass;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class DynamicResourceConfigTest extends TestCase {
    
    public DynamicResourceConfigTest(String testName) {
        super(testName);
    }
    
    public void testTopLevel() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/ws/rest/impl/container/config/toplevel"};
        p.put(ResourceConfig.PROPERTY_RESOURCE_PATHS, paths);
        ResourceConfig rc = new DynamicResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testInnerStatic() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/ws/rest/impl/container/config/innerstatic"};
        p.put(ResourceConfig.PROPERTY_RESOURCE_PATHS, paths);
        ResourceConfig rc = new DynamicResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testTopLevelInnerStatic() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/ws/rest/impl/container/config/toplevelinnerstatic"};
        p.put(ResourceConfig.PROPERTY_RESOURCE_PATHS, paths);
        ResourceConfig rc = new DynamicResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    public void testAll() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/ws/rest/impl/container/config"};
        p.put(ResourceConfig.PROPERTY_RESOURCE_PATHS, paths);
        ResourceConfig rc = new DynamicResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(4, rc.getResourceClasses().size());
    }
    
    
    public void testJarTopLevel() throws IOException {
        File jarFile = createJarFile("build/test/classes/",
                "com/sun/ws/rest/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/ws/rest/impl/container/config/toplevel/PackageRootResourceClass.class"
                );
        ResourceConfig rc = createConfigFromJar(jarFile);

        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testJarInnerStatic() throws IOException {
        File jarFile = createJarFile("build/test/classes/",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfigFromJar(jarFile);

        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testJarBoth() throws IOException {
        File jarFile = createJarFile("build/test/classes/",
                "com/sun/ws/rest/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/ws/rest/impl/container/config/toplevel/PackageRootResourceClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/ws/rest/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfigFromJar(jarFile);

        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    private ResourceConfig createConfigFromJar(File jarFile) {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = new String[1];
        paths[0] = jarFile.getAbsolutePath();
        p.put(ResourceConfig.PROPERTY_RESOURCE_PATHS, paths);
        return new DynamicResourceConfig(p);        
    }
    
    private File createJarFile(String base, String... entries) throws IOException {
        File tempJar = File.createTempFile("test", "jar");
        tempJar.deleteOnExit();
        JarOutputStream jos = new JarOutputStream(
                new BufferedOutputStream(
                new FileOutputStream(tempJar)));
        
        for (String entry : entries) {
            JarEntry e = new JarEntry(entry);
            jos.putNextEntry(e);

            InputStream f = new BufferedInputStream(
                    new FileInputStream(base + entry));
            byte[] buf = new byte[1024];
            int read = 1024;
            while ((read = f.read(buf, 0, read)) != -1 ) {
                jos.write(buf, 0, read);
            }
            jos.closeEntry();
        }
        
        jos.close();
        return tempJar;
    }
}

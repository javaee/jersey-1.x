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

import com.sun.ws.rest.api.core.ClasspathResourceConfig;
import com.sun.ws.rest.api.core.ResourceConfig;
import com.sun.ws.rest.impl.container.config.innerstatic.InnerStaticClass;
import com.sun.ws.rest.impl.container.config.toplevel.PublicRootResourceClass;
import com.sun.ws.rest.impl.container.config.toplevelinnerstatic.PublicRootResourceInnerStaticClass;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ClasspathResourceConfigTest extends AbstractResourceConfigTester {
    
    public ClasspathResourceConfigTest(String testName) {
        super(testName);
    }
    
    public void testTopLevel() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/ws/rest/impl/container/config/toplevel"};
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testInnerStatic() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/ws/rest/impl/container/config/innerstatic"};
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testTopLevelInnerStatic() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/ws/rest/impl/container/config/toplevelinnerstatic"};
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    public void testAll() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/ws/rest/impl/container/config"};
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(4, rc.getResourceClasses().size());
    }
    
    public void testAllWithOnePath() {
        Map<String, Object> p = new HashMap<String, Object>();
        String paths = 
                "build/test/classes/com/sun/ws/rest/impl/container/config/toplevel;" +
                "build/test/classes/com/sun/ws/rest/impl/container/config/innerstatic;" +
                "build/test/classes/com/sun/ws/rest/impl/container/config/toplevelinnerstatic";
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
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
        ResourceConfig rc = createConfig(jarFile);

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
        ResourceConfig rc = createConfig(jarFile);

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
        ResourceConfig rc = createConfig(jarFile);

        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    private ResourceConfig createConfig(File jarFile) {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = new String[1];
        paths[0] = jarFile.getAbsolutePath();
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        return new ClasspathResourceConfig(p);        
    }    
}

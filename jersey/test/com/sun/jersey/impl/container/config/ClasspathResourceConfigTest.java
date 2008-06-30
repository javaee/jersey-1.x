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

import com.sun.jersey.api.core.ClasspathResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.container.config.innerstatic.InnerStaticClass;
import com.sun.jersey.impl.container.config.toplevel.PublicRootResourceClass;
import com.sun.jersey.impl.container.config.toplevelinnerstatic.PublicRootResourceInnerStaticClass;
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
        String[] paths = {"build/test/classes/com/sun/jersey/impl/container/config/toplevel"};
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testInnerStatic() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/jersey/impl/container/config/innerstatic"};
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testTopLevelInnerStatic() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/jersey/impl/container/config/toplevelinnerstatic"};
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    public void testAll() {
        Map<String, Object> p = new HashMap<String, Object>();
        String[] paths = {"build/test/classes/com/sun/jersey/impl/container/config"};
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
                "build/test/classes/com/sun/jersey/impl/container/config/toplevel;" +
                "build/test/classes/com/sun/jersey/impl/container/config/innerstatic;" +
                "build/test/classes/com/sun/jersey/impl/container/config/toplevelinnerstatic";
        p.put(ClasspathResourceConfig.PROPERTY_CLASSPATH, paths);
        ResourceConfig rc = new ClasspathResourceConfig(p);
        
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.class));
        assertTrue(rc.getResourceClasses().contains(PublicRootResourceInnerStaticClass.PublicClass.class));
        assertEquals(4, rc.getResourceClasses().size());
    }
    
    public void testAllWithOnePathWithSpacesAndEmptyElements() {
        Map<String, Object> p = new HashMap<String, Object>();
        String paths = 
                "  build/test/classes/com/sun/jersey/impl/container/config/toplevel  ;" +
                "  build/test/classes/com/sun/jersey/impl/container/config/innerstatic  ;" +
                "  build/test/classes/com/sun/jersey/impl/container/config/toplevelinnerstatic; ;; ; ";
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
                "com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class"
                );
        ResourceConfig rc = createConfig(jarFile);

        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testJarInnerStatic() throws IOException {
        File jarFile = createJarFile("build/test/classes/",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(jarFile);

        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(1, rc.getResourceClasses().size());
    }
    
    public void testJarBoth() throws IOException {
        File jarFile = createJarFile("build/test/classes/",
                "com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(jarFile);

        assertTrue(rc.getResourceClasses().contains(PublicRootResourceClass.class));
        assertTrue(rc.getResourceClasses().contains(InnerStaticClass.PublicClass.class));
        assertEquals(2, rc.getResourceClasses().size());
    }
    
    public void testJarAzZipBoth() throws IOException {
        File zipFile = createJarFile(Suffix.zip, "build/test/classes/",
                "com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class",
                "com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
                "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
                );
        ResourceConfig rc = createConfig(zipFile);

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

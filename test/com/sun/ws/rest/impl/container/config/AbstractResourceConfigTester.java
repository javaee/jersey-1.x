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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import junit.framework.*;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class AbstractResourceConfigTester extends TestCase {
    
    public AbstractResourceConfigTester(String testName) {
        super(testName);
    }
    
    public final File createJarFile(String base, String... entries) throws IOException {
        File tempJar = File.createTempFile("test", ".jar");
        tempJar.deleteOnExit();
        JarOutputStream jos = new JarOutputStream(
                new BufferedOutputStream(
                new FileOutputStream(tempJar)), new Manifest());
        
        Set<String> usedSegments = new HashSet<String>();
        for (String entry : entries) {
            for (String path : getPaths(entry)) {
                if (usedSegments.contains(path))
                    continue;
                
                usedSegments.add(path);
                JarEntry e = new JarEntry(path);
                jos.putNextEntry(e);
                jos.closeEntry();                
            }
            
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
    
    private String[] getPaths(String entry) {
        String[] segments = entry.split("/");
        String[] paths = new String[segments.length - 1];
        
        if (paths.length == 0)
            return paths;
        
        paths[0] = segments[0] + "/";
        for (int i = 1; i < paths.length; i++) {
            paths[i] = "";
            for (int j = 0; j <= i; j++) {
                paths[i] += segments[j] + "/";
            }
        }
            
        return paths;
    }
}

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

package com.sun.ws.rest.samples.atomserver.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class FileStore {
    public static final FileStore FS = new FileStore();

    public String getNextId() {
        return UUID.randomUUID().toString();
    }
    
    public boolean exists(String path) {
        return new File(path).exists();
    }
    
    public InputStream getFileContents(String path) {
        try {
            return new BufferedInputStream(new FileInputStream(path));
        } catch (FileNotFoundException e) {
            return null;
        }
    }
 
    public OutputStream getFileOutputStream(String path) throws IOException {
        File f = new File(path);
        f.getParentFile().mkdirs();
        return new BufferedOutputStream(new FileOutputStream(f));
    }
    
    public void deleteFile(String path) {
        new File(path).delete();
    }
    
    public void deleteDirectory(String path) {        
        File dir = new File(path);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
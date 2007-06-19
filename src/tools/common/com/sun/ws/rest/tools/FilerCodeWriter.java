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

package com.sun.ws.rest.tools;

import com.sun.codemodel.JPackage;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.mirror.apt.Filer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Writes all the source files using the specified Filer.
 * 
 * @author Doug Kohlert
 */
public class FilerCodeWriter extends FileCodeWriter {

    /** The Filer used to create files. */
    private final Filer filer;
    
    private Writer w;
    
    public FilerCodeWriter(File outDir, Filer filer) throws IOException {
        super(outDir);
        this.filer = filer;
    }
    
    
    public Writer openSource(JPackage pkg, String fileName) throws IOException {
        String tmp = fileName.substring(0, fileName.length()-5);
        w = filer.createSourceFile(pkg.name()+"."+tmp);
        return w;
    }
    

    public void close() throws IOException {
        super.close();
        if (w != null)
            w.close();
        w = null;
    }
}

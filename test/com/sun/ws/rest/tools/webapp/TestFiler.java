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

package com.sun.ws.rest.tools.webapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.apt.Filer.Location;
/**
 *
 * @author Doug Kohlert
 */
public class TestFiler implements Filer {
    ByteArrayOutputStream out;
    
    /** Creates a new instance of TestFiler */
    public TestFiler() {
        out = new ByteArrayOutputStream();
    }
 
    public OutputStream createBinaryFile(Location loc, String pkg, File relPath) {
        return out;
    }
    
    public OutputStream createClassFile(String name) {
        return out;
    }
    
    public PrintWriter createSourceFile(String name) {
        return new PrintWriter(out);
    }
    
    public PrintWriter createTextFile(Location loc, String pkg, File relPath, String charsetName) {
        return new PrintWriter(out);
    }

    public String getOutputString() {
        return out.toString();
    }
}

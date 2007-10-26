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

package com.sun.ws.rest.impl.provider.entity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class FileProvider extends AbstractTypeEntityProvider<File> {
    
    public boolean supports(Class type) {
        return type == File.class;
    }

    public File readFrom(Class<File> type, MediaType mediaType,
            MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException {
        File f = File.createTempFile("rep","tmp");        
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        try {
            writeTo(entityStream, out);
        } finally {
            out.close();
        }
        return f;
    }

    public void writeTo(File t, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream entityStream) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(t));
        try {
            writeTo(in, entityStream);
        } finally {
            in.close();
        }
    }
}

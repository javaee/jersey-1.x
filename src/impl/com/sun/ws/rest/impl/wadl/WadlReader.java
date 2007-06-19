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

package com.sun.ws.rest.impl.wadl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for working with WADL files
 */
public class WadlReader {
    
    public static final String BASE_URI_POSITION_MARKER = "%%REPLACE%%";
    
    public static String read(InputStream wadlFile, String baseUri) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(wadlFile));
        StringBuffer output = new StringBuffer();
        Pattern pattern = Pattern.compile(BASE_URI_POSITION_MARKER);
        Matcher matcher = pattern.matcher("");
        // scan WADL file line by line and substitute base URI where
        // indicated
        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                matcher.reset(line);
                line = matcher.replaceAll(baseUri);
                output.append(line);
                output.append('\n');
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return output.toString();
    }
    
    public static String read(InputStream wadlFile, URI baseUri) {
        return read(wadlFile, baseUri.toString());
    }
}

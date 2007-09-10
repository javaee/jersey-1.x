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

package com.sun.ws.rest.samples.entityprovider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.EntityProvider;

/**
 * Entity provider for encoding and decoding java.util.Properties.
 *
 * Exercise for the reader: modify this provider to support the additional
 * encoding/decoding of to/from XML if the MIME type is XML-based.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class PropertiesProvider implements EntityProvider<Properties> {
    
    public boolean supports(Class<?> type) {
        // Only support the Properties classe and inherited classes of
        return Properties.class.isAssignableFrom(type);
    }
        
    public Properties readFrom(Class<Properties> type, MediaType mediaType, 
            MultivaluedMap<String, String> headers, InputStream in) throws IOException {
        // Create a new Properties instance and load using the
        // key/value pair format
        Properties p = new Properties();
        p.load(in);
        return p;
    }
    
    public void writeTo(Properties p, MediaType mediaType,
            MultivaluedMap<String, Object> headers, OutputStream out) throws IOException {
        // Store the Properties instance using the key/value pair format
        p.store(out, null);
    }
}

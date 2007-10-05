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

package com.sun.ws.rest.impl.test.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.URLConnection;
import org.codehaus.jettison.json.JSONArray;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public class AuxContentHandlerFactory implements ContentHandlerFactory {
    
    // in fact, JSONArray is returned even for JSONObjects
    // then JSONObject is encapsulated as a member of one-item array
    class JSONObjectContentHandler extends ContentHandler {
        public Object getContent(URLConnection con) {
            try {
                InputStream is = con.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write('[');
                byte[] buf = new byte[1024];
                int read;
                while ((read = is.read(buf)) != -1) {
                    baos.write(buf, 0, read);
                }
                baos.write(']');
                return new JSONArray(baos.toString()).get(0);
            } catch (Exception ioe) {
                ioe.printStackTrace(System.err);
                return null;
            }
        }
    }
    
    class StringContentHandler extends ContentHandler {
        public Object getContent(URLConnection con) {
            try {
                InputStream is = con.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int read;
                while ((read = is.read(buf)) != -1) {
                    baos.write(buf, 0, read);
                }
                return baos.toString();
            } catch (Exception ioe) {
                ioe.printStackTrace(System.err);
                return null;
            }
        }
    }
    
    
    /** Creates a new instance of AuxContentHandlerFactory */
    public AuxContentHandlerFactory() {
    }
    
    public ContentHandler createContentHandler(String mimetype) {
        if ("application/json".equals(mimetype)) {
            return new JSONObjectContentHandler();
        } else if ("application/xml".equals(mimetype)) { 
            // TODO: maybe return something more appropriate than just String for xml ?
            return new StringContentHandler();     
        } else if ("text/plain".equals(mimetype)) {
            return new StringContentHandler();
        }
        return null;
    }
}

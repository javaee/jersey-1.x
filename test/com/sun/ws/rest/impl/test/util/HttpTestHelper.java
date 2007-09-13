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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Jakub Podlesak (japod at sun dot com)
 */
public class HttpTestHelper {
    
    public final static class HttpResponse {
        public Object content;
        public int code;
        public String message;
    }
    
    public static HttpResponse makeHttpRequest(String method, String url, String contentType, InputStream is) throws Exception {
        HttpResponse response = new HttpResponse();
        URL urlAddress = new URL(url);
        HttpURLConnection huc = (HttpURLConnection) urlAddress.openConnection();
        huc.setRequestMethod(method);
        if (null != is) {
            huc.setDoOutput(true);
            huc.setRequestProperty("Content-Type", contentType);
            OutputStream os = huc.getOutputStream();
            byte[] buf = new byte[1024];
            int read;
            while ((read = is.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
        }
        response.code = huc.getResponseCode();
        response.message = huc.getResponseMessage();
        if (HttpURLConnection.HTTP_NO_CONTENT != response.code) {
            response.content = huc.getContent();
        }
        return response;
    }
    
    public static HttpResponse makeHttpRequest(String method, String url, String contentType, String content) throws Exception {
        return makeHttpRequest(method, url, contentType, new ByteArrayInputStream(content.getBytes("UTF-8")));
    }
    
    public static HttpResponse makeHttpRequest(String method, String url) throws Exception {
        return makeHttpRequest(method, url, null, (InputStream)null);
    }
    
}

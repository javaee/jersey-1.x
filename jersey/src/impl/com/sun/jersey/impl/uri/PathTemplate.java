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

package com.sun.jersey.impl.uri;

import com.sun.jersey.api.uri.UriTemplate;
import com.sun.jersey.api.uri.UriComponent;

/**
 * A URI template for a URI path.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public final class PathTemplate extends UriTemplate {
    
    /**
     * Create a URI path template and encode (percent escape) any characters 
     * of the template that are not valid URI characters.
     * 
     * @param template the URI path template
     */
    public PathTemplate(String template) {
        this(template, false);
    }
    
    /**
     * Create a URI path template and encode (percent escape) any characters 
     * of the template that are not valid URI characters.
     * 
     * @param template the URI path template. The template is prefixed with
     *        a '/' if the template does not start with a '/'.
     * @param encode if true encode (percent escape) any characters of the 
     *       template that are not valid URI path characters (excluding template
     *       specific characters), otherwise validate the characters.
     * @throws IllegalArgumentException if the template is validated and it
     *         contains illegal characters. 
     */
    public PathTemplate(String template, boolean encode) {
        super(encodeOrValidate(prefixWithSlash(template), encode));
    }
    
    private static String encodeOrValidate(String path, boolean encode) {
        if (encode) {
            return UriComponent.encode(path, UriComponent.Type.PATH, true);
        } else {
            UriComponent.validate(path, UriComponent.Type.PATH, true);
            return path;
        }
    }
    
    private static String prefixWithSlash(String regex) {
        return (!regex.startsWith("/")) ? "/" + regex : regex;
    }
}
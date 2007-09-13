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

package com.sun.ws.rest.spi.dispatch;

import com.sun.ws.rest.api.core.UriComponent;
import javax.ws.rs.UriTemplate;

/**
 * A URI template for a URI path.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class UriPathTemplate extends UriTemplateType {
    
    /**
     * Create a URI template from the UriTemplate annotation.
     *
     * @param template the URI template annotation. If template.value() does
     *        does not start with a '/' then the path is modified to start with
     *        a '/'
     */
    public UriPathTemplate(UriTemplate template) {
        this(prefixWithSlash(template.value()), 
                template.limited(), template.encode());
    }
    
    /**
     * Create a URI template from the UriTemplate annotation.
     *
     * @param template the URI template annotation. If template.value() does
     *        does not start with a '/' then the path is modified to start with
     *        a '/'
     * @param limited overrides the value declared in template.      
     */
    public UriPathTemplate(UriTemplate template, boolean limited) {
        this(prefixWithSlash(template.value()), 
                limited, template.encode());
    }
    
    /**
     * Create a URI template from a URI path containing template variables.
     * <p>
     * This is equivalent to calling the constructor 
     * UriPathTemplate(path, true, true).
     *
     * @param path the URI path.
     */
    public UriPathTemplate(String path) {
        this(path, true, true);
    }
    
    /**
     * Create a URI template from a URI path containing template variables.
     * <p>
     * This is equivalent to calling the constructor 
     * UriPathTemplate(path, limited, true).
     *
     * @param path the URI path.
     * @param limited if true the right hand expression "(/)?" is appended 
     *        to the regular expression generated from the URI path,
     *        otherwise the expression "(/.*)?" is appended.
     */
    public UriPathTemplate(String path, boolean limited) {
        this(path, limited, true);
    }
    
    /**
     * Create a URI template from a URI path containing template variables.
     * <p>
     * The URI path is assumed to be decoded form and will be encoded.
     *
     * @param path the URI path.
     * @param limited if true the right hand expression "(/)?" is appended 
     *        to the regular expression generated from the URI path,
     *        otherwise the expression "(/.*)?" is appended.
     * @param encode 
     */
    public UriPathTemplate(String path, boolean limited, boolean encode) {
        super(encodeOrValidate(prefixWithSlash(path), encode), limited);
    }    

    private static String encodeOrValidate(String path, boolean encode) {
        if (encode) {
            return UriComponent.encode(path, UriComponent.Type.PATH, true);
        } else {
            UriComponent.validate(path, UriComponent.Type.PATH, true);
            return path;
        }
    }

    private static String prefixWithSlash(String path) {
        return (!path.startsWith("/")) ? "/" + path : path;
    }
}

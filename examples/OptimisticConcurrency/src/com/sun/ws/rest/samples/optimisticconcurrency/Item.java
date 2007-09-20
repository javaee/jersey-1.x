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

package com.sun.ws.rest.samples.optimisticconcurrency;

import java.net.URI;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Item {
    private String uri;
    
    private String updateUri;
    
    private String mediaType;
    
    public Item() {
    }

    public Item(String uri, String updateUri) {
        setUri(uri);
        setUpdateUri(updateUri);
    }
    
    public Item(URI uri, URI updateUri, String mediaType) {
        setUri(uri.toString());
        setUpdateUri(updateUri.toString());
        setMediaType(mediaType);
    }
    
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUpdateUri() {
        return updateUri;
    }

    public void setUpdateUri(String updateUri) {
        this.updateUri = updateUri;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    
}

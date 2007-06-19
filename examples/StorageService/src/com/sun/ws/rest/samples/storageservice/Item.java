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

package com.sun.ws.rest.samples.storageservice;

import java.util.GregorianCalendar;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Item {
    private String name; 
    
    private String uri;
    
    private String mimeType;

    private GregorianCalendar lastModified;
    
    private String digest;
    
    public Item() {
    }
    
    public Item(String name, String uri) {
        setName(name);
        setUri(uri);
    }
    
    public Item(String name, String uri, String mimeType, GregorianCalendar lastModified) {
        setName(name);
        setUri(uri);
        setMimeType(mimeType);
        setLastModified(lastModified);
    }    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public GregorianCalendar getLastModified() {
        return lastModified;
    }

    public void setLastModified(GregorianCalendar lastModified) {
        this.lastModified = lastModified;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }
}

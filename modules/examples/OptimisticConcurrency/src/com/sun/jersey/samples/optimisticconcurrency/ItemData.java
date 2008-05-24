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

package com.sun.jersey.samples.optimisticconcurrency;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ItemData {
    public static final JAXBContext CONTEXT = getContext();
    
    private static JAXBContext getContext() {
        try {
            return JAXBContext.newInstance(Item.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        return null;
    }
  
    public static ItemData ITEM = new ItemData();
        
    public int version = 0;
    
    public MediaType mediaType;
    
    public byte[] content = "Today is the first day of the REST of my life".getBytes();
    
    public ItemData() {
        mediaType = new MediaType("text", "plain");
    }
    
    public int getVersion() {
        return version;
    }
    
    public String getVersionAsString() {
        return Integer.toString(version);
    }
    
    public MediaType getMediaType() {
        return mediaType;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    public void update(byte[] content) {
        update(new MediaType("application", "octet-stream"), content);
    }
    
    public synchronized void update(MediaType mediaType, byte[] content) {
        this.mediaType = mediaType;
        this.content = (content != null) ? content : new byte[0];
        version++;
    }
}

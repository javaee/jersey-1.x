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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Container {
    private String name;
    
    private String uri;
    
    private List<Item> item; 
    
    public Container() {
    }
    
    public Container(String name, String uri) {
        setName(name);
        setUri(uri);
        setItem(new ArrayList<Item>());
    }
    
    public Container(String name, String uri, List<Item> items) {
        setName(name);
        setUri(uri);
        setItem(items);
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
    
    public List<Item> getItem() {
        return item;
    }

    
    public void setItem(List<Item> items) {
        this.item = items;
    }
    
    public Item getItem(String name) {
        for (Item i : item)
            if (i.getName().equals(name))
                return i;
    
        return null;
    }
    
    public void putItem(Item item) {
        ListIterator<Item> i = getItem().listIterator();
        while (i.hasNext())
            if (i.next().getName().equals(item.getName())) {
                i.set(item);
                return;
            }
        
        getItem().add(item);
    }
    
    public Item removeItem(String name) {
        ListIterator<Item> i = getItem().listIterator();
        while (i.hasNext()) {
            Item item = i.next();
            if (item.getName().equals(name)) {
                i.remove();
                return item;
            }
        }
        
        return null;
    }

    public Container clone() {
        Container that = new Container(this.name, this.uri);
        that.setItem(new ArrayList<Item>(this.item));
        
        return that;
    }
}
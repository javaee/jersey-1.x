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

package com.sun.ws.rest.impl.json.reader;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;


/**
 *
 * @author japod
 */
public abstract class JsonReaderXmlEvent {
    
    public static class Attribute {
        String name;
        String value;
        
        public Attribute(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    Location location;
    QName name;
    String text;
    List<Attribute> attributes;
    
    public abstract int getEventType();

    public boolean isAttribute() {
        return false;
    }

    public boolean isCharacters() {
        return false;
    }
    
    public boolean isEndDocument() {
        return false;
    }

    public boolean isEndElement() {
        return false;
    }

    public boolean isEntityReference() {
        return false;
    }

    public boolean isNamespace() {
        return false;
    }

    public boolean isProcessingInstruction() {
        return false;
    }

    public boolean isStartDocument() {
        return false;
    }

    public boolean isStartElement() {
        return false;
    }

    public int getAttributeCount() {
        return (null != attributes) ? attributes.size() : 0;
    }
    
    public String getAttributeLocalName(int index) {
        if ((null == attributes) || (index >= attributes.size())) {
            throw new IndexOutOfBoundsException();
        }
        return attributes.get(index).name;
    }
    
    public QName getAttributeName(int index) {
        return new QName(null, getAttributeLocalName(index));
    }    

    public String getAttributePrefix(int index) {
        if ((null == attributes) || (index >= attributes.size())) {
            throw new IndexOutOfBoundsException();
        }
        return null;
    }

    public String getAttributeType(int index) {
        return null;
    }

    public String getAttributeNamespace(int index) {
        return null;
    }
    
    public String getAttributeValue(int index) {
        if ((null == attributes) || (index >= attributes.size())) {
            throw new IndexOutOfBoundsException();
        }
        return attributes.get(index).value;
    }
    
    public String getAttributeValue(String namespaceURI, String localName) {
        if ((null == attributes) || (null == localName) || ("".equals(localName))) {
            throw new NoSuchElementException();
        }
        for (Attribute a : attributes) {
            if (localName.equals(a.name)) {
                return a.value;
            }
        }
        throw new NoSuchElementException();
    }
    
    public boolean isAttributeSpecified(int index) {
        return (null != attributes) && (attributes.size() >= index);
    }
    
    public String getText() {
        if (null != text) {
            return text;
        } else {
            throw new IllegalStateException();
        }
    }
    
    public char[] getTextCharacters() {
        if (null != text) {
            return text.toCharArray();
        } else {
            throw new IllegalStateException();
        }
    }
    
    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        if (null != text) {
            System.arraycopy(text.toCharArray(), sourceStart, target, targetStart, length);
            return length;
        } else {
            throw new IllegalStateException();
        }
    }
    
    public int getTextStart() {
        if (null != text) {
            return 0;
        } else {
            throw new IllegalStateException();
        }
    }
    
    public int getTextLength() {
        if (null != text) {
            return text.length();
        } else {
            throw new IllegalStateException();
        }
    }
    
    public boolean hasName() {
        return null != name;
    }
    
    public QName getName() {
        if (null != name) {
            return name;
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    public String getLocalName() {
        if (null != name) {
            return name.getLocalPart();
        } else {
            throw new UnsupportedOperationException();
        }
    }
    
    public String getPrefix() {
        if (null != name) {
            return name.getPrefix();
        } else {
            return null;
        }
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void addAttribute(String name, String value) {
        if (null == attributes) {
            attributes = new LinkedList<JsonReaderXmlEvent.Attribute>();
        }
        attributes.add(new Attribute(name, value));
    }
}

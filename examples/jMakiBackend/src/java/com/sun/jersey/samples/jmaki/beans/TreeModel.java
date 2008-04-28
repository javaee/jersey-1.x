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

package com.sun.jersey.samples.jmaki.beans;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author japod
 */
@XmlRootElement
public class TreeModel {
    
    public static class Node {
        @XmlElement  public String label;
        @XmlElement public boolean expanded;
        @XmlElement public List<Node> children;
        
        public Node(){}

        public Node(String label){
            this(label, null);
        }
        
        public Node(String label, Collection<Node> children) {
            this.label = label;
            if (null != children) {
                this.children = new LinkedList<Node>();
                this.children.addAll(children);
                expanded = true;
            }
        }
        
        @Override
        public int hashCode() {
            int result = 13;
            result = 5 + 17 * label.hashCode();
            if (null != children) {
                for (Node n : children) {
                    result = 5 + 17 * n.hashCode();
                }
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) {
                return false;
            }
            final Node other = (Node) obj;
            if (this.label != other.label && (this.label == null || !this.label.equals(other.label))) {
                return false;
            }
            if (this.children != other.children && (this.children == null || !this.children.equals(other.children))) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            String result = "(" + label + ":";
            if (null != children) {
                for (Node n : children) {
                    result += n.toString();
                }
                return result + ")";
            } else {
                return result + "0 children)";
            }
        }
    }
    
    @XmlElement public Node root;
    
    public TreeModel() {}
    
    public TreeModel(Node root) {
        this.root = root;
    }
    
    @Override
    public int hashCode() {
        if (null != root) {
            return 7 + root.hashCode();
        } else {
            return 7;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TreeModel)) {
            return false;
        }
        final TreeModel other = (TreeModel) obj;
        if (this.root != other.root && (this.root == null || !this.root.equals(other.root))) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return (null != root) ? root.toString() : "(NULL_ROOT)";
    }
}

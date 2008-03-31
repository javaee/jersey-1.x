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

package com.sun.ws.rest.impl.subresources;

import com.sun.ws.rest.impl.AbstractResourceTester;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AncestorsTest extends AbstractResourceTester {
    
    public AncestorsTest(String testName) {
        super(testName);
    }
    
    @Path("/node")
    static public class Node { 
        int n = 0;
        
        public Node() {}
        
        private Node(int i) { this.n = i; }
        
        @Path("node") public Node getChild() {            
            return new Node(n + 1);
        }        
        
        @GET public String get(@Context UriInfo ui) {
            assertEquals(n + 1, ui.getAncestorResources().size());
            assertEquals(n + 1, ui.getAncestorResourceURIs().size());
            
            for (int i = 0; i <= n; i++) {
                Node node = (Node)ui.getAncestorResources().get(i);
                assertEquals(n - i, node.n);
            }
            
            for (int i = 0; i <= n; i++) {
                String p = ui.getAncestorResourceURIs().get(i);
                assertEquals(getPath(n - i), p);
            }
            
            return Integer.toString(n);
        }
        
        @Path("leaf") @GET public String getSub(@Context UriInfo ui) {
            assertEquals(n + 1 + 1, ui.getAncestorResources().size());
            assertEquals(n + 1 + 1, ui.getAncestorResourceURIs().size());
            
            Node node = (Node)ui.getAncestorResources().get(0);
            assertEquals(n, node.n);            
            for (int i = 0; i <= n; i++) {
                node = (Node)ui.getAncestorResources().get(i + 1);
                assertEquals(n - i, node.n);
            }
            
            String p = ui.getAncestorResourceURIs().get(0);
            assertEquals(getPathLeaf(n), p);            
            for (int i = 0; i <= n; i++) {
                p = ui.getAncestorResourceURIs().get(i + 1);
                assertEquals(getPath(n - i), p);
            }
            
            return Integer.toString(n);        
        }
        
        protected String getPath(int n) {
            String p = "node";
            for (int i = 1; i <= n; i++)
                p += "/node";
            return p;
        }
        
        protected String getPathLeaf(int n) {
            return getPath(n) + "/leaf";
        }        
    }
    
    
    public void testNode() {
        initiateWebApplication(Node.class);
        
        assertEquals("0", resource("/node").get(String.class));
        assertEquals("1", resource("/node/node").get(String.class));
        assertEquals("2", resource("/node/node/node").get(String.class));
        assertEquals("3", resource("/node/node/node/node").get(String.class));    
    }    
    
    public void testNodeLeaf() {
        initiateWebApplication(Node.class);
        
        assertEquals("0", resource("/node/leaf").get(String.class));
        assertEquals("1", resource("/node/node/leaf").get(String.class));
        assertEquals("2", resource("/node/node/node/leaf").get(String.class));
        assertEquals("3", resource("/node/node/node/node/leaf").get(String.class));    
    }
    
    @Path("/node/")
    static public class NodeSlash { 
        int n = 0;
        
        public NodeSlash() {}
        
        private NodeSlash(int i) { this.n = i; }
        
        @Path("node/") public NodeSlash getChild() {            
            return new NodeSlash(n + 1);
        }
        
        @GET public String get(@Context UriInfo ui) {
            assertEquals(n + 1, ui.getAncestorResources().size());
            assertEquals(n + 1, ui.getAncestorResourceURIs().size());
            
            for (int i = 0; i <= n; i++) {
                NodeSlash node = (NodeSlash)ui.getAncestorResources().get(i);
                assertEquals(n - i, node.n);
            }
            
            for (int i = 0; i <= n; i++) {
                String p = ui.getAncestorResourceURIs().get(i);
                assertEquals(getPath(n - i), p);
            }
            
            return Integer.toString(n);
        }
        
        @Path("leaf/") @GET public String getSub(@Context UriInfo ui) {
            assertEquals(n + 1 + 1, ui.getAncestorResources().size());
            assertEquals(n + 1 + 1, ui.getAncestorResourceURIs().size());
            
            NodeSlash node = (NodeSlash)ui.getAncestorResources().get(0);
            assertEquals(n, node.n);            
            for (int i = 0; i <= n; i++) {
                node = (NodeSlash)ui.getAncestorResources().get(i + 1);
                assertEquals(n - i, node.n);
            }
            
            String p = ui.getAncestorResourceURIs().get(0);
            assertEquals(getPathLeaf(n), p);            
            for (int i = 0; i <= n; i++) {
                p = ui.getAncestorResourceURIs().get(i + 1);
                assertEquals(getPath(n - i), p);
            }
            
            return Integer.toString(n);        
        }
        
        protected String getPath(int n) {
            String p = "node/";
            for (int i = 1; i <= n; i++)
                p += "node/";
            return p;
        }
        
        protected String getPathLeaf(int n) {
            return getPath(n) + "leaf/";
        }
    }
    
    public void testNodeSlash() {
        initiateWebApplication(NodeSlash.class);
        
        assertEquals("0", resource("/node/").get(String.class));
        assertEquals("1", resource("/node/node/").get(String.class));
        assertEquals("2", resource("/node/node/node/").get(String.class));
        assertEquals("3", resource("/node/node/node/node/").get(String.class));    
    }    
    
    public void testNodeLeafSlash() {
        initiateWebApplication(NodeSlash.class);
        
        assertEquals("0", resource("/node/leaf/").get(String.class));
        assertEquals("1", resource("/node/node/leaf/").get(String.class));
        assertEquals("2", resource("/node/node/node/leaf/").get(String.class));
        assertEquals("3", resource("/node/node/node/node/leaf/").get(String.class));    
    }    
}
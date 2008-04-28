/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */


package com.sun.jersey.api.model;

/**
 * Abstraction for various issues of a resource model validity like: 
 * no resources, duplicated uri templates, etc.
 *
 * @author japod
 */
public class ResourceModelIssue {
    
    Object source;
    String message;
    boolean fatal;

    public ResourceModelIssue(Object source, String message) {
        this(source, message, false);
    }
    
    public ResourceModelIssue(Object source, String message, boolean fatal) {
        this.source = source;
        this.message = message;
        this.fatal = fatal;
    }
    
    /** Human readible description of the issue */
    public String getMessage() {
        return this.message;
    }
    
    /** If this returns true, appropriate abstract resource has a fatal issue */
    public boolean isFatal() {
        return this.fatal;
    }
    
    /** Corresponding object (having the issue) */
    public Object getSource() {
        return this.source;
    }
}

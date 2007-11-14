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


package com.sun.ws.rest.api.model;

/**
 * Abstraction for various issues of a resource model validity like: 
 * no resources, duplicated uri templates, etc.
 *
 * @author japod
 */
public class ResourceModelIssue {
    
    String message;
    boolean fatal;

    /** Human readible description of the issue */
    public String getMessage() {
        return this.message;
    }
    
    /** If this returns true, appropriate resource model has a fatal issue */
    public boolean isFatal() {
        return this.fatal;
    }
}

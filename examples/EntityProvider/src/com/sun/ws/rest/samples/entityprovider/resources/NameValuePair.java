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

/*
 * NameValuePair.java
 *
 * Created on November 21, 2007, 11:20 AM
 *
 */

package com.sun.ws.rest.samples.entityprovider.resources;

/**
 *
 * @author mh124079
 */
public class NameValuePair {
    
    private String name;
    private String value;
    
    /** Creates a new instance of NameValuePair */
    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
    
}

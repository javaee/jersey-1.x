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
 * UriPathValue.java
 *
 * Created on October 5, 2007, 11:51 AM
 *
 */

package com.sun.ws.rest.api.model;

/**
 * Abstraction for a URI path value
 */
public class UriPathValue {
    
    private String value;
    private boolean encode;
    private boolean limited;
    
    /** Creates a new instance of UriPathValue */
    public UriPathValue(String path) {
        this(path, true);
    }
    
    /** Creates a new instance of UriPathValue */
    public UriPathValue(String path, boolean encode) {
        this(path, encode, true);
    }

    /** Creates a new instance of UriPathValue */
    public UriPathValue(String path, boolean encode, boolean limited) {
        this.value = path;
        this.encode = encode;
        this.limited = limited;
    }

    public String getValue() {
        return value;
    }

    public boolean isEncode() {
        return encode;
    }

    public boolean isLimited() {
        return limited;
    }
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName() 
                + "(" 
                + ((null == getValue())? "null" : ("\"" + getValue() + "\"")) 
                + ")";
    }
}

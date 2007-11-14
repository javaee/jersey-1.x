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
 * UriTemplateValue.java
 *
 * Created on October 5, 2007, 11:51 AM
 *
 */

package com.sun.ws.rest.api.model;

import java.util.regex.Pattern;

/**
 * Abstraction for a UriTemplate value
 */
public class UriTemplateValue {
    
    private String rawTemplate;
    private boolean encode;
    private boolean limited;
    
    /** Creates a new instance of UriTemplateValue */
    public UriTemplateValue(String template) {
        this(template, true);
    }
    
    /** Creates a new instance of UriTemplateValue */
    public UriTemplateValue(String template, boolean encode) {
        this(template, encode, true);
    }

    /** Creates a new instance of UriTemplateValue */
    public UriTemplateValue(String template, boolean encode, boolean limited) {
        this.rawTemplate = template;
        this.encode = encode;
        this.limited = limited;
    }

    public String getRawTemplate() {
        return rawTemplate;
    }

    public boolean isEncode() {
        return encode;
    }

    public boolean isLimited() {
        return limited;
    }

    public String getEncodedTemplate() {
        // TODO: implement - ignore template parameters when encoding
        throw new UnsupportedOperationException();
    }
    public Pattern getRegExp() {
        // TODO: implement - substitute matching patterns for template params
        throw new UnsupportedOperationException();
    }
}

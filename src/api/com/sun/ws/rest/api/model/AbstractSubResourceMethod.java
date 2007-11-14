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
 * AbstractSubResourceMethod.java
 *
 * Created on October 5, 2007, 1:34 PM
 *
 */

package com.sun.ws.rest.api.model;

import java.lang.reflect.Method;

/**
 *
 * @author mh124079
 */
public class AbstractSubResourceMethod extends AbstractResourceMethod implements UriTemplated {
    
    private UriTemplateValue uriTemplate;
    
    /**
     * Creates a new instance of AbstractSubResourceMethod
     */
    public AbstractSubResourceMethod(Method method, UriTemplateValue uriTemplate) {
        super(method);
        this.uriTemplate = uriTemplate;
    }

    public AbstractSubResourceMethod(Method method, UriTemplateValue uriTemplate, String httpMethod) {
        super(method, httpMethod);
        this.uriTemplate = uriTemplate;
    }
    
    public UriTemplateValue getUriTemplate() {
        return uriTemplate;
    }
    
}

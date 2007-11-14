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
 * AbstractSubResourceLocator.java
 *
 * Created on October 5, 2007, 1:45 PM
 *
 */

package com.sun.ws.rest.api.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction for a sub resource locator
 */
public class AbstractSubResourceLocator implements UriTemplated, Parameterized {
    
    private UriTemplateValue uriTemplate;
    private Method method;
    private List<Parameter> parameters;
    
    /**
     * Creates a new instance of AbstractSubResourceLocator
     */
    public AbstractSubResourceLocator(Method method, UriTemplateValue uriTemplate) {
        this.method = method;
        this.uriTemplate =  uriTemplate;
        this.parameters = new ArrayList<Parameter>();
    }

    public UriTemplateValue getUriTemplate() {
        return uriTemplate;
    }
    
    public Method getMethod() {
        return method;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }
}

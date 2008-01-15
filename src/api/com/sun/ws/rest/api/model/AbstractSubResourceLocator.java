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
public class AbstractSubResourceLocator implements UriPathAnnotated, Parameterized, AbstractModelComponent {
    
    private UriPathValue uriPath;
    private Method method;
    private List<Parameter> parameters;
    
    /**
     * Creates a new instance of AbstractSubResourceLocator
     */
    public AbstractSubResourceLocator(Method method, UriPathValue uriPath) {
        
        assert null != method;
        
        this.method = method;
        this.uriPath =  uriPath;
        this.parameters = new ArrayList<Parameter>();
    }

    public UriPathValue getUriPath() {
        return uriPath;
    }
    
    public Method getMethod() {
        return method;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void accept(AbstractModelVisitor visitor) {
        visitor.visitAbstractSubResourceLocator(this);
    }

    public List<AbstractModelComponent> getComponents() {
        return null;
    }
    
    @Override
    public String toString() {
        return "AbstractSubResourceLocator(" 
                + getMethod().getDeclaringClass().getSimpleName() + "#" + getMethod().getName() + ")";
    }
}

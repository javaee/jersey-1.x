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
 * AbstractResourceMethod.java
 *
 * Created on October 5, 2007, 11:46 AM
 *
 */
package com.sun.ws.rest.api.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;

/**
 * Abstraction for a resource method
 */
public class AbstractResourceMethod implements Parameterized, AbstractModelComponent {
    private AbstractResource resource;
    private List<MediaType> consumeMimeList;
    private List<MediaType> produceMimeList;
    private List<Parameter> parameters;
    private String httpMethod;
    private Method method;

    public AbstractResourceMethod(AbstractResource resource, 
            Method method, String httpMethod) {
        
        assert null != method;

        this.resource = resource;
        this.method = method;
        this.httpMethod = httpMethod;
        this.consumeMimeList = new ArrayList<MediaType>();
        this.produceMimeList = new ArrayList<MediaType>();
        this.parameters = new ArrayList<Parameter>();
    }

    public AbstractResource getDeclaringResource() {
        return resource;
    }
    
    public List<MediaType> getSupportedInputTypes() {
        return consumeMimeList;
    }

    public List<MediaType> getSupportedOutputTypes() {
        return produceMimeList;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Method getMethod() {
        return method;
    }
    
    public void accept(AbstractModelVisitor visitor) {
        visitor.visitAbstractResourceMethod(this);
    }
    
    public List<AbstractModelComponent> getComponents() {
        return null;
    }

    @Override
    public String toString() {
        return "AbstractResourceMethod(" 
                + getMethod().getDeclaringClass().getSimpleName() + "#" + getMethod().getName() + ")";
    }

}

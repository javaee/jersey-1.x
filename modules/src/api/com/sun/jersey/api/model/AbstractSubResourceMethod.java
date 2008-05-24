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
package com.sun.jersey.api.model;

import java.lang.reflect.Method;

/**
 *
 * @author mh124079
 */
public class AbstractSubResourceMethod extends AbstractResourceMethod implements UriPathAnnotated {

    private UriPathValue uriPath;

    public AbstractSubResourceMethod(AbstractResource resource,
            Method method, UriPathValue uriPath, String httpMethod) {
        super(resource, method, httpMethod);
        this.uriPath = uriPath;
    }

    public UriPathValue getUriPath() {
        return uriPath;
    }

    @Override
    public void accept(AbstractModelVisitor visitor) {
        visitor.visitAbstractSubResourceMethod(this);
    }
    
    @Override
    public String toString() {
        return "AbstractSubResourceMethod(" 
                + getMethod().getDeclaringClass().getSimpleName() + "#" + getMethod().getName() + ")";
    }
}

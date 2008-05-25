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
 * AbstractResourceConstructor.java
 *
 * Created on October 5, 2007, 3:21 PM
 *
 */

package com.sun.jersey.api.model;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction for a resource class constructor
 */
public class AbstractResourceConstructor implements Parameterized, AbstractModelComponent {
    
    private Constructor ctor;
    private List<Parameter> parameters;
    
    /**
     * Creates a new instance of AbstractResourceConstructor
     */
    public AbstractResourceConstructor(Constructor constructor) {
        this.ctor = constructor;
        parameters = new ArrayList<Parameter>();
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public Constructor getCtor() {
        return ctor;
    }

    public void accept(AbstractModelVisitor visitor) {
        visitor.visitAbstractResourceConstructor(this);
    }

    public List<AbstractModelComponent> getComponents() {
        return null;
    }
    
}

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
 * AbstractWebAppModel.java
 *
 * Created on October 5, 2007, 11:33 AM
 *
 */

package com.sun.ws.rest.api.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Abstraction for the set of resources that make up an application
 * 
 */
public class AbstractWebAppModel {

    private List<AbstractResource> rootResources;
    private List<AbstractResource> subResources;
    
    /**
     * Creates a new instance of AbstractWebAppModel
     */
    public AbstractWebAppModel() {
        rootResources = new ArrayList<AbstractResource>();
        subResources = new ArrayList<AbstractResource>();
    }
    
    public List<AbstractResource> getRootResources() {
        return rootResources;
    }

    public List<AbstractResource> getSubResources() {
        return subResources;
    }
}

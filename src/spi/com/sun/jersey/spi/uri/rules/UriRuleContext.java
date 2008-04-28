/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.jersey.spi.uri.rules;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.uri.UriTemplate;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface UriRuleContext extends HttpContext {

    /**
     * Get the resource instance from a resource class.
     * 
     * @param resourceClass the resource class
     * @return the resource instance
     */
    Object getResource(Class resourceClass);
    
    /**
     * Get the rules for a resource class.
     * 
     * @param resourceClass the resource class that has rules
     * @return the rules
     */
    UriRules<UriRule> getRules(Class resourceClass);

    /**
     * Get the list to store the values of a pattern's 
     * capturing groups.
     * 
     * @return the list to store capturing group values.
     */
    List<String> getGroupValues();
    
    /**
     *  Set template values that are the values of a pattern's
     *  capturing groups.
     * 
     *  @param names the template names associated with the capturing group
     *         values.
     */
    void setTemplateValues(List<String> names);
    
    /**
     * Push the resource and matching URI template associated with the resource.
     * 
     * @param resource
     */
    void pushResource(Object resource, UriTemplate template);

    /**
     * Push the right hand path length to calculate the entry for
     * the list of matching (ancestor) URI paths.
     * 
     * @param rhpathlen the right hand length
     */
    void pushRightHandPathLength(int rhpathlen);
}
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

package com.sun.ws.rest.api.view;

/**
 * A viewable type referencing a template by name and a model to be passed
 * to the template. Such a type may be returned by an HTTP method of a 
 * resource class. In this respect the template is the view and the controller 
 * is the resource class in the Model View Controller pattern.
 * <p>
 * When the viewable type is processed the template name will be resolved 
 * to an fully qualifed template path (if resolvable) and the template 
 * identified by that path will be processed.
 * <p>
 * TODO specify template name to absolute template path to fully qualified 
 * template path.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class Viewable {

    private final String templateName;
    
    private final Object model;

    /**
     * Construct a new viewable type with a template name and a model.
     * 
     * @param templateName the template name.
     * @param model the model.
     */
    public Viewable(String templateName, Object model) {
        this.templateName = templateName;
        this.model = model;
    }
       
    /**
     * Get the template name.
     * 
     * @return the template name.
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * Get the model.
     * 
     * @return the model.
     */
    public Object getModel() {
        return model;
    }    
}
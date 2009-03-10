/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.api.view;

import com.sun.jersey.spi.template.TemplateContext;

/**
 * A viewable type referencing a template by name and a model to be passed
 * to the template. Such a type may be returned by a resource method of a 
 * resource class. In this respect the template is the view and the controller 
 * is the resource class in the Model View Controller pattern.
 * <p>
 * The template name may be declared as absolute template name if the name
 * begins with a '/', otherwise the template name is declared as a relative
 * template name.
 * <p>
 * A relative template name requires resolving to an absolute template name
 * when the viewable type is processed. 
 * 
 * If a resolving class is present then that class will be used to resolve the
 * relative template name.
 * 
 * If a resolving class is not present then the class of the last matching
 * resource obtained from {@link javax.ws.rs.core.UriInfo#getMatchedResources() },
 * namely the class obtained from the expression
 * <code>uriInfo.getMatchedResources().get(0).getClass()</code>, is utilized
 * as the resolving class. 
 * 
 * If there are no matching resources then an error will result.
 * 
 * <p>
 * The resolving class, and super classes in the inheritence hierarchy, are
 * utilized to generate the absolute template name as follows.
 *
 * The base path starts with '/' character, followed by the fully
 * qualified class name of the resolving class, with any '.' and '$' characters
 * replaced with a '/' character, followed by a '/' character,
 * followed by the relative template name.
 *
 * If the absolute template name cannot be resolved into a fully qualified
 * template name (see {@link TemplateContext}) then the super class of the
 * resolving class is utilized, and is set as the resolving class. Traversal up
 * the inheritence hierarchy proceeds until an absolute template name can be
 * resolved into a fully qualified template name, or the Object class is
 * reached, which means the absolute template name could not be resolved and
 * an error will result.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class Viewable {

    private final String templateName;
    
    private final Object model;

    private final Class<?> resolvingClass;

    /**
     * Construct a new viewable type with a template name and a model.
     * 
     * @param templateName the template name, shall not be null.
     * @param model the model, may be null.
     */
    public Viewable(String templateName, Object model) {
        this(templateName, model, null);
    }
       
    /**
     * Construct a new viewable type with a template name, a model
     * and a resolving class.
     *
     * @param templateName the template name, shall not be null.
     * @param model the model, may be null.
     * @param resolvingClass the class to use to resolve the template name
     *        if the template is not absolute, if null then the resolving
     *        class will be obtained from the last matching resource.
     * @throws IllegalArgumentException if the template name is null.
     */
    public Viewable(String templateName, Object model, Class<?> resolvingClass) 
            throws IllegalArgumentException {
        if (templateName == null)
            throw new IllegalArgumentException("The template name MUST not be null");

        this.templateName = templateName;
        this.model = model;
        this.resolvingClass = resolvingClass;
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

    /**
     * Get the resolving class.
     *
     * @return the resolving class.
     */
    public Class<?> getResolvingClass() {
        return resolvingClass;
    }

    /**
     * 
     * @return true if the template name is absolute, and starts with a
     *         '/' character
     */
    public boolean isTemplateNameAbsolute() {
        return templateName.length() > 0 && templateName.charAt(0) == '/';
    }
}
/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.spi.template;

import com.sun.jersey.api.view.Viewable;
import javax.ws.rs.core.UriInfo;

/**
 * The context for resolving an instance of {@link Viewable} to
 * an instance of {@link ResolvedViewable}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface TemplateContext {
    
    /**
     * Resolve a {@link Viewable}.
     * <p>
     * If the template name of the viewable is not absolute then the resolving
     * class of the viewable is utilized to resolve the relative template name
     * into an absolute template name. 
     * 
     * If the resolving class is not set (a null value) then the class of the
     * model is utilized as the resolving class.
     *
     * If the model is not set (a null value) then a {@link TemplateContextException}
     * is thrown.
     *
     * @param v the viewable
     * @return the resolved viewable
     * @throws TemplateContextException if the viewable cannot be resolved.
     */
    ResolvedViewable resolveViewable(Viewable v) throws TemplateContextException;

    /**
     * Resolve a {@link Viewable}.
     * <p>
     * If the template name of the viewable is not absolute then the resolving
     * class of the viewable is utilized to resolve the relative template name
     * into an absolute template name.
     *
     * If the resolving class is not set (a null value) then the class of the
     * last matching resource obtained from
     * {@link javax.ws.rs.core.UriInfo#getMatchedResources() }, namely
     * the class obtained from the expression
     * <code>uriInfo.getMatchedResources().get(0).getClass()</code>, is utilized
     * as the resolving class.
     *
     * If there are no matching resoruces then a {@link TemplateContextException}
     * is thrown.
     *
     * @param v the viewable
     * @param ui
     * @return the resolved viewable
     * @throws TemplateContextException if the viewable cannot be resolved.
     */
    ResolvedViewable resolveViewable(Viewable v, UriInfo ui) throws TemplateContextException;

    /**
     * Resolve a {@link Viewable} given a resolving class.
     * <p>
     * If the template name of the viewable is not absolute then the resolving
     * class of the viewable is utilized to resolve the relative template name
     * into an absolute template name.
     *
     * If the resolving class is not set (a null value) then the class of the
     * <code>resolvingClass</code> parameter is utilized as the resolving class.
     *
     * If the <code>resolvingClass</code> parameter is null then a
     * {@link TemplateContextException} is thrown.
     *
     * @param v the viewable.
     * @param resolvingClass the resolving class to use if the resolving
     *        class of the viewable is not set.
     * @return the resolved viewable.
     * @throws TemplateContextException if the viewable cannot be resolved.
     */
    ResolvedViewable resolveViewable(Viewable v, Class<?> resolvingClass) throws TemplateContextException;
}
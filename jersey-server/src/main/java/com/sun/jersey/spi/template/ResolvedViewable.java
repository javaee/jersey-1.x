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

package com.sun.jersey.spi.template;

import com.sun.jersey.api.view.Viewable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A resolved {@link Viewable}.
 * <p>
 * A resolved viewable is obtained from the resolving methods on
 * {@link TemplateContext} and has associated with it a {@link TemplateProcessor}
 * that is capable of processing the template, referenced by the template name,
 * that is a fully qualified name as produced from
 * {@link TemplateProcessor#resolve(java.lang.String)}.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ResolvedViewable extends Viewable {
    
    private final TemplateProcessor template;

    /**
     * Create a resolved viewable.
     * 
     * @param t the template processor.
     * @param fullyQualifiedName the fully qualified template name identifying a
     *        template.
     * @param model the model.
     */
    public ResolvedViewable(TemplateProcessor t, String fullyQualifiedName, Object model) {
        this(t, fullyQualifiedName, model, null);
    }

    /**
     * Create a resolved viewable.
     * 
     * @param t the template processor.
     * @param fullyQualifiedName the fully qualified template name identifying a
     *        template.
     * @param model the model.
     * @param resolvingClass the resolving class that was used to resolve a
     *        relative template name into an absolute template name.
     */
    public ResolvedViewable(TemplateProcessor t, String fullyQualifiedName, Object model, Class<?> resolvingClass) {
        super(fullyQualifiedName, model, resolvingClass);
        
        this.template = t;
    }
    
    /**
     * Write to the template, that is referenced by the fully qualified
     * template name.
     * <p>
     * The model of the resolved viewable is passed to the template.
     * <p>
     * This method defers to {@link TemplateProcessor#writeTo(java.lang.String, java.lang.Object, java.io.OutputStream) }.
     * 
     * @param out the output stream that the template processor writes
     *        the processing of the template referenced by the fully qualified
     *        template name.
     * 
     * @throws java.io.IOException
     */
    public void writeTo(OutputStream out) throws IOException {
        template.writeTo(getTemplateName(), getModel(), out);
    }
}
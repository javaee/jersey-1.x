/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * {@link TemplateContext} and has associated with it a {@link ViewProcessor}
 * that is capable of processing a template identified by a template reference.
 * 
 * @param <T> the type of the resolved template object.
 * @author Paul.Sandoz@Sun.Com
 */
public class ResolvedViewable<T> extends Viewable {
    
    private final ViewProcessor<T> vp;

    private final T templateObject;
    
    /**
     * Create a resolved viewable.
     *
     * @param vp the view processor that resolved a template name to a template
     *        reference.
     * @param t the template reference.
     * @param v the viewable that is resolved.
     */
    public ResolvedViewable(ViewProcessor<T> vp, T t, Viewable v) {
        this(vp, t, v, null);
    }

    /**
     * Create a resolved viewable.
     *
     * @param vp the view processor that resolved a template name to a template
     *        reference.
     * @param t the template reference.
     * @param v the viewable that is resolved.
     * @param resolvingClass the resolving class that was used to resolve a
     *        relative template name into an absolute template name.
     */
    public ResolvedViewable(ViewProcessor<T> vp, T t, Viewable v, Class<?> resolvingClass) {
        super(v.getTemplateName(), v.getModel(), resolvingClass);

        this.vp = vp;
        this.templateObject = t;
    }

    /**
     * Write the resolved viewable.
     * <p>
     * This method defers to {@link ViewProcessor#writeTo(java.lang.Object, com.sun.jersey.api.view.Viewable, java.io.OutputStream)  }
     * to write the viewable utilizing the template reference.
     * 
     * @param out the output stream that the view processor writes to.
     * 
     * @throws java.io.IOException
     */
    public void writeTo(OutputStream out) throws IOException {
        vp.writeTo(templateObject, this, out);
    }
}
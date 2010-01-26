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

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.view.Viewable;
import java.io.IOException;
import java.io.OutputStream;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.Provider;

/**
 * A view processor.
 * <p>
 * Implementations of this interface shall be capable of resolving a
 * template name to a template reference that identifies a template supported
 * by the implementation. And, processing the template, identified by template
 * reference, the results of which are written to an output stream.
 * <p>
 * Implementations can register a view processor as a provider, for
 * example, annotating the implementation class with {@link Provider}
 * or registering an implementing class or instance as a singleton with
 * {@link ResourceConfig} or {@link Application}.
 * <p>
 * Such view processors could be JSP view processors (supported by the
 * Jersey servlet and filter implementations) or say Freemarker or Velocity
 * view processors (not implemented).
 * 
 * @param <T> the type of the template object.
 * @author Paul.Sandoz@Sun.Com
 */
public interface ViewProcessor<T> {
   
    /**
     * Resolve a template name to a template reference.
     * 
     * @param name the template name
     * @return the template reference, otherwise null
     *         if the template name cannot be resolved.
     */
    T resolve(String name);
    
    /**
     * Process a template and write the result to an output stream.
     * 
     * @param t the template reference. This is obtained by calling the
     *        {@link #resolve(java.lang.String)} method with a template name.
     * @param viewable the viewable that contains the model to be passed to the
     *        template.
     * @param out the output stream to write the result of processing the
     *        template.
     * @throws java.io.IOException if there was an error processing the
     *         template.
     */
    void writeTo(T t, Viewable viewable, OutputStream out) throws IOException;
}
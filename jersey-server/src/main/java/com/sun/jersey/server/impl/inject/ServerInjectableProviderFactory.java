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
package com.sun.jersey.server.impl.inject;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.service.ComponentContext;
import com.sun.jersey.core.spi.factory.InjectableProviderFactory;
import com.sun.jersey.spi.service.AnnotationObjectContext;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ServerInjectableProviderFactory extends InjectableProviderFactory
            implements ServerInjectableProviderContext {
    
    public Injectable getInjectable(Parameter p, Scope s) {
        if (p.getAnnotation() == null) return null;
        
        ComponentContext ic = new AnnotationObjectContext(p.getAnnotations());
        
        if (s == Scope.PerRequest) {
            // Find a per request injectable with Parameter
            Injectable i = getInjectable(p.getAnnotation().annotationType(), ic, p.getAnnotation(), 
                    p, Scope.PerRequest);
            if (i != null) return i;

            // Find a per request, undefined or singleton injectable with parameter Type
            return getInjectable(
                    p.getAnnotation().annotationType(), 
                    ic, 
                    p.getAnnotation(), 
                    p.getParameterType(),
                    Scope.PERREQUEST_UNDEFINED_SINGLETON
                    );
        } else {
            // Find a undefined or singleton injectable with parameter Type
            return getInjectable(
                    p.getAnnotation().annotationType(), 
                    ic, 
                    p.getAnnotation(), 
                    p.getParameterType(),
                    Scope.UNDEFINED_SINGLETON
                    );            
        }
    }
    
    public List<Injectable> getInjectable(List<Parameter> ps, Scope s) {
        List<Injectable> is = new ArrayList<Injectable>();
        for (Parameter p : ps)
            is.add(getInjectable(p, s));
        return is;
    }
}
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.server.impl.inject;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.inject.Injectable;
import java.util.List;
import javax.ws.rs.WebApplicationException;


/**
 * A hold of a list of injectable that obtains the injectable values
 * from that list.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class InjectableValuesProvider {

    private final List<AbstractHttpContextInjectable> is;

    /**
     * Create a new instance given a list of injectable.
     * 
     * @param is the list of injectable.
     */
    public InjectableValuesProvider(List<Injectable> is) {
        this.is = AbstractHttpContextInjectable.transform(is);
    }

    public List<AbstractHttpContextInjectable> getInjectables() {
        return is;
    }

    /**
     * Get the injectable values.
     *
     * @param context the http contest.
     * @return the injectable values. Each element in the object array
     *         is a value obtained from the injectable at the list index
     *         that is the element index.
     */
    public Object[] getInjectableValues(HttpContext context) {
        final Object[] params = new Object[is.size()];
        try {
            int index = 0;
            for (AbstractHttpContextInjectable i : is) {
                params[index++] = i.getValue(context);
            }
            return params;
        } catch (WebApplicationException e) {
            throw e;
        } catch (ContainerException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ContainerException("Exception obtaining parameters", e);
        }
    }
}

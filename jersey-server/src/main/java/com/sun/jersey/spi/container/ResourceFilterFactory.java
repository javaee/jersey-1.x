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
package com.sun.jersey.spi.container;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import java.util.List;

/**
 * A resource filter factory responsible for creating {@link ResourceFilter}
 * instances that match methods of the abstract resource model.
 * <p>
 * Resource filter factories are registered with the {@link ResourceConfig}
 * using the property {@link ResourceConfig#PROPERTY_RESOURCE_FILTER_FACTORIES}.
 * 
 * @author Paul.Sandoz@Sun.Com
 * @see com.sun.jersey.api.container.filter
 */
public interface ResourceFilterFactory {

    /**
     * Create a list of {@link ResourceFilter} instance given a method
     * of the abstract resource model.
     * <p>
     * When applying the list of resource filters to a request each resource filter
     * is applied, in order, from the first to last entry in the list.
     * When applying the list of resource filters to a response each resource filter
     * is applied, in reverse order, from the last to first entry in the list.
     *
     * @param am the abstract method. This may be an instance
     *        of the following: {@link AbstractResourceMethod},
     *        {@link AbstractSubResourceMethod} or {@link AbstractSubResourceLocator}.
     * @return the list of resource filter, otherwise an empty list or null if
     *         no resource filters are associated with the method.
     */
    List<ResourceFilter> create(AbstractMethod am);
}
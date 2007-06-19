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

package com.sun.ws.rest.impl.container.servlet;

import com.sun.ws.rest.api.core.ResourceConfig;
import java.util.HashSet;
import java.util.Set;

public class WebResources
    implements ResourceConfig
{
    private Set<Class> resources = new HashSet<Class>();

    public WebResources() {
        resources.add(com.sun.ws.rest.impl.container.servlet.MyWebResourceBean.class);
    }

    public Set<Class> getResourceClasses() {
        return resources;
    }

    public boolean isIgnoreMatrixParams() {
        return true;
    }

    public boolean isRedirectToNormalizedURI() {
        return true;
    }

}

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

package com.sun.ws.rest.api.core;

import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.core.MediaType;

/**
 * An extension of {@link DefaultResourceConfig} that adapts an instance
 * of {@link ApplicationConfig}.
 */
public final class ApplicationConfigAdapter extends DefaultResourceConfig {
    
    private final ApplicationConfig ac;
    
    /**
     * @param ac the application config
     */
    public ApplicationConfigAdapter(ApplicationConfig ac) {
        this.ac = ac;
    }
    

    @Override
    public Set<Class<?>> getResourceClasses() {
        return ac.getResourceClasses();
    }

    @Override
    public Set<Class<?>> getProviderClasses() {
        return ac.getProviderClasses();
    }
    
    @Override
    public Map<String, MediaType> getExtensionMappings() {
        return ac.getExtensionMappings();
    }

    @Override
    public Map<String, String> getLanguageMappings() {
        return ac.getLanguageMappings();
    }
}
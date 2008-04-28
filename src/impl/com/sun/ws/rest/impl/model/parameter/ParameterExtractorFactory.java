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

package com.sun.ws.rest.impl.model.parameter;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.model.AbstractResourceConstructor;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.Parameter;
import com.sun.ws.rest.impl.ImplMessages;
import java.util.List;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class ParameterExtractorFactory {
    
    private ParameterExtractorFactory() {
    }
    
    /**
     * Create parameter extractors for a Java method that is a sub-locator.
     *
     * @param m the Java method
     * @return the array of parameter extractors
     */
    public static ParameterExtractor[] createExtractorsForSublocator(AbstractSubResourceLocator subResLocator) {
        ParameterExtractor[] extractors = processParameters(subResLocator);
        for (ParameterExtractor extractor: extractors) {
            if (extractor == null) {
                // This requires better message
                String msg = ImplMessages.NOT_VALID_DYNAMICRESOLVINGMETHOD(subResLocator.getMethod(),
                        "",
                        subResLocator.getMethod().getDeclaringClass());
                throw new ContainerException(msg);
            }
        }
        return extractors;
    }
    
    /**
     * Create parameter extractors for the constructor of a resource class.
     *
     * @param ctor the constructor of a resource class
     * @return the array of parameter extractors
     */
    public static ParameterExtractor[] createExtractorsForConstructor(AbstractResourceConstructor aRCtor) {
        return processParameters(aRCtor.getParameters());
    }
    
    
    private static ParameterExtractor[] processParameters(AbstractSubResourceLocator subResLocator) {
        return processParameters(subResLocator.getParameters());
    }
    
    
    private static ParameterExtractor[] processParameters(List<Parameter> parameters) {
        
        ParameterExtractor[] extractors = new ParameterExtractor[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            extractors[i] = processParameter(parameters.get(i));
        }
        
        return extractors;
    }
    
    @SuppressWarnings("unchecked")
    private static ParameterExtractor processParameter(Parameter parameter) {
        
        ParameterProcessor p = ParameterProcessorFactory.createParameterProcessor(parameter.getSource());
        if (null == p) {
            return null;
        }
        return p.process(parameter);
    }
}
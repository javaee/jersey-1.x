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


package com.sun.jersey.spi.template;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A processor for templates.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public interface TemplateProcessor {
   
    /**
     * Resolve an abstract template path into a fully qualified concrete
     * template path that identifies a template.
     * 
     * @param path the abstract template path 
     * @return the fully qualified concrete template path, otherwise null
     *         if the abstract template path cannot be resolved.
     */
    String resolve(String path);
    
    /**
     * Process a template and write the result to an output stream.
     * 
     * @param resolvedPath the resolved path identifying a template. This
     *        is obtained by calling the resolve method with an abstract 
     *        template path.
     * @param model the model to be passed to the template.
     * @param out the output stream to write the result of processing the
     *        template.
     * @throws java.io.IOException if there was an error processing the
     *         template.
     */
    void writeTo(String resolvedPath, Object model, OutputStream out) throws IOException;
}
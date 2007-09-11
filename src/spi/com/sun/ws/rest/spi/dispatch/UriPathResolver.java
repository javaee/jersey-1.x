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

package com.sun.ws.rest.spi.dispatch;

import java.util.List;
import java.util.Map;

/**
 * Resolve a URI path to an instance of a specified type, T, from
 * given a set of the URI templates. Each URI template is associated with a
 * particular instance of T.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface UriPathResolver<T> {
    /**
     * Add a template to the set of templates to resolve.
     *
     * @param template the URI template
     * @param t the instance of a type associated with the template to be
     *        returned if a URI is resolved to the template.
     * @return true if the template was added, otherwise false if there is a duplicate
     *         template in the list.
     */
    boolean add(UriTemplateType template, T t);
    
    /**
     * Get the URI templates currently added to the resolver.
     *
     * @return the URI templates.
     */
    Map<UriTemplateType, T> getUriTemplates();
    
    /**
     * Resolve a URI path to an instance of a specified type, T.
     * <p>
     * This method can be used when the template variables
     * of a URI template are separated from the URI template instance.
     *
     * @param path the URI path
     * @param rightHandPath the returned right hand path of the path if resolving
     *        succeeded.
     * @param templateValues the returned linear list of template values matching
     *        the template variables
     * @return T the instance of a type associated with the resolved URI path, otherwise
     *         null if the path could not be resolved
     */
    T resolve(CharSequence path, StringBuilder rightHandPath, List<String> templateValues);
    
    /**
     * Resolve a URI path to an instance of a specified type, T.
     *
     * @param path the URI path
     * @param rightHandPath the returned right hand path of the path if resolving
     *        succeeded.
     * @param templateValues the returned map of template variables to template values.
     * @return T the instance of a type associated with the resolved URI path, otherwise
     *         null if the path could not be resolved
     */
    T resolve(CharSequence path, StringBuilder rightHandPath, Map<String, String> templateValues);
}

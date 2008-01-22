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

package com.sun.ws.rest.spi;

import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * A provider that supports the conversion of an HTTP header, of type T, to and
 * from a {@link String}.
 * <p>
 * An implementation (a service-provider) identifies itself by placing a 
 * provider-configuration file (if not already present), 
 * "com.sun.ws.rest.spi.HeaderDelegateProvider" in the 
 * resource directory <tt>META-INF/services</tt>, and including the fully qualified
 * service-provider-class of the implementation in the file.

 * @author Paul.Sandoz@Sun.Com
 */
public interface HeaderDelegateProvider<T> extends HeaderDelegate<T> {

    /**
     * Ascertain if the Provider supports a particular type.
     *
     * @param type the type that is to be supported.
     * @return true if the type is supported, otherwise false.
     */
    boolean supports(Class<?> type);
}

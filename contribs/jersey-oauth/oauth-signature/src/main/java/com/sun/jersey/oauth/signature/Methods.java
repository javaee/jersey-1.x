/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.oauth.signature;

import java.util.HashMap;
import com.sun.jersey.spi.service.ServiceFinder;

/**
 * Loads and provides instances of OAuth signature methods.
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 */
class Methods {

    /** Contains the loaded service provider classes. */
    private static final HashMap<String, OAuthSignatureMethod> methods = loadMethods();

    /**
     * Returns the OAuth method for a given method name.
     *
     * @param name the name of the OAuth method to get instance of.
     * @return the method, or null if no such method could be loaded.
     */
    public static OAuthSignatureMethod getInstance(String name) {
        return methods.get(name);
    }

    /**
     * Returns as hash map of loaded OAuth methods.
     */
    private static HashMap<String, OAuthSignatureMethod> loadMethods() {
        HashMap<String, OAuthSignatureMethod> map = new HashMap<String, OAuthSignatureMethod>();
        for (OAuthSignatureMethod method : ServiceFinder.find(OAuthSignatureMethod.class, true)) {
            map.put(method.name(), method);
        }
        return map;
    } 
}


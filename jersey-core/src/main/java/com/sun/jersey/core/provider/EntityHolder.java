/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.core.provider;

/**
 * A holder for an entity of a request consumed by the server or a response
 * consumed by a client.
 * <p>
 * Some clients/servers may send requests/responses with no entity or an
 * entity (for example WebDAV clients). To support such request or responses
 * The <code>EntityHolder</code> type may be used where the actual type
 * of the entity is declared as a type parameter.
 * <p>
 * For example a POST method that consumes a specific JAXB object,
 * <code>MyJAXBObject</code>, can determine if the request contained the
 * JAXB object or the request contained no entity, as follows:
 * <blockquote><pre>
 *     &#64;POST
 *     &#64;Consumes("text/plain")
 *     public void post(EntityHolder&lt;MyJAXBObject&gt; s) {
 *         if (s.hasEntity()) {
 *             MyJAXBObject entity = s.getEntity();
 *         } else {
 *           ...
 *         }
 *     }
 * </pre></blockquote>
 * <p>
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class EntityHolder<T> {
    private final T t;

    /**
     * Create an entity holder containing no entity.
     */
    public EntityHolder() {
        this.t = null;
    }

    /**
     * Create an entity holder containing an entity.
     *
     * @param t the entity.
     */
    public EntityHolder(T t) {
        this.t = t;
    }

    /**
     *
     * @return true if there is an entity present, otherwise false.
     */
    public boolean hasEntity() {
        return t != null;
    }

    /**
     * Get the entity.
     * @return the entity if present, otherwise null if the entity is not
     *         present.
     */
    public T getEntity() {
        return t;
    }
}

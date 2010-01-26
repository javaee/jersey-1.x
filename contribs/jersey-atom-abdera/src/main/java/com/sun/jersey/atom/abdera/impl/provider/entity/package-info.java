/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

/**
 * <h3>JAX-RS AtomPub Provider Classes</h3>
 *
 * <p>This package contains JAX-RS Provider classes for the media types
 * defined by the Atom Publishing Protocol.</p>
 *
 * <p>In general, applications should not need to reference these classes
 * directly.  Therefore, backwards compatibility for APIs within this package
 * should <strong>NOT</strong> be assumed across versions of Jersey.</p>
 *
 * <p>This module leverages the data model and parsing classes from the
 * <a href="http://abdera.apache.org" target="_new">Apache Abdera Project</a>
 * to convert between the internal (Java object) and external (XML or JSON)
 * representations of the specified Abdera model APIs:</p>
 * <ul>
 *   <li>{@link org.apache.abdera.model.Service} - AtomPub Service Document.</li>
 *   <li>{@link org.apache.abdera.model.Categories} - AtomPub Category Document.</li>
 *   <li>{@link org.apache.abdera.model.Feed} - Atom Feed Document.</li>
 *   <li>{@link org.apache.abdera.model.Entry} - Atom Entry Document.</li>
 * </ul>
 *
 * <p>In general, response representations in either XML (standard Atom or
 * AtomPub formats) or JSON (transliteration of Atom/AtomPub format to XML)
 * may be requested, via the HTTP <code>Accept</code> header.  See the class
 * Javadocs for each provider class (and review the value specified for the
 * JAX-RS {@link javax.ws.rs.Consumes} and {@link javax.ws.rs.Produces}
 * annotations) for the exact capabilities of each provider.</p>
 *
 */

package com.sun.jersey.atom.abdera.impl.provider.entity;

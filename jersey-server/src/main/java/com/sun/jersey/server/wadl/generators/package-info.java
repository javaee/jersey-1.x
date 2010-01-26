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
 * Provides support for JAXB WADL generation.
 * <p>
 * The different {@link com.sun.jersey.server.wadl.WadlGenerator} implementations
 * provide the possibility to add some content stored in a file to the generated wadl.
 * </p>
 * <p>
 * The content can either be provided via a {@link java.io.File} reference,
 * or via a resource that will be read as an {@link java.io.InputStream}.
 * </p>
 * <p>
 * The {@link java.io.File} reference is appropriate when generating wadl offline,
 * e.g. with the help of the
 * <a href="https://jersey.dev.java.net/source/browse/jersey/trunk/jersey/contribs/maven-wadl-plugin/">maven-wadl-plugin</a>
 * (see the <a href="https://jersey.dev.java.net/source/browse/jersey/trunk/jersey/samples/generate-wadl/">generate-wadl sample</a>).
 * <br/>
 * The {@link java.io.InputStream} is appropriate, when the wadl is generated from a jersey
 * application running in some servlet container or application server.
 * </p>
 */
package com.sun.jersey.server.wadl.generators;

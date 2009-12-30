/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Provides support for Model, View and Controller (MVC).
 * <p>
 * Given the MVC pattern the Controller corresponds to a resource class,
 * the View to a template referenced by a template name, and the Model
 * to a Java object (or a Java bean).
 * <p>
 * A resource method of a resource class may return an instance of 
 * {@link com.sun.jersey.api.view.Viewable} that encapsulates the template name 
 * and the model. In this respect the instance of
 * {@link com.sun.jersey.api.view.Viewable} is the response entity. Such a
 * viewable response entity may be set in contexts other than a resource
 * method but for the purposes of this section the focus is on resource methods.
 * <p>
 * The {@link com.sun.jersey.api.view.Viewable}, returned by a resource method,
 * will be processed such that the template name is resolved to a template
 * reference that identifies a template capable of being processed by an
 * appropriate view processor.
 * 
 * The view processor then processes template given the model to produce a
 * response entity that is returned to the client.
 * <p>
 * For example, the template name could reference a Java Server Page (JSP) and
 * the model will be accessible to that JSP. The JSP view processor will
 * process the JSP resulting in an HTML document that is returned
 * as the response entity. (See later for more details.)
 * <p>
 * Two forms of returning {@link com.sun.jersey.api.view.Viewable} instances 
 * are supported: explicit; and implicit.
 *
 * <h2>Explicit views</h2>
 *
 * <h2>Implicit views</h2>
 *
 * <h2>Integration with Java Server Pages (JSPs)</h2>
 *
 * <h2>Developing a View Processor</h2>
 */
package com.sun.jersey.api.view;

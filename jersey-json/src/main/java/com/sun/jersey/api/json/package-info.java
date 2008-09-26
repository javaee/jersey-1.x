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
 * Provides support for enabling and configuring JSON.
 * <p>
 * The JSON API allows you to customize the JSON format produced/consumed
 * with JAXB bean entities. All you need is to provide your own implementation
 * of {@link javax.ws.rs.ext.ContextResolver} and make it return a pre-configured
 * {@link com.sun.jersey.api.json.JSONJAXBContext}.
 * <p>
 * The JSON API can be used as follows to configure other than the default JSON format:
 * <blockquote><pre>
 * <span style="font-weight:bold">&#064;Provider</span>
 * public final class JAXBContextResolver <span style="font-weight:bold">implements ContextResolver&lt;JAXBContext&gt;</span> {
 *
 *   <span style="font-weight:bold">private final JAXBContext context;</span>
 *
 *   private final Set&lt;Class&gt; types;
 *
 *   private final Class[] cTypes = {BeanOne.class, BeanTwo.class};
 *
 *   public JAXBContextResolver() throws Exception {
 *       Map&lt;String, Object&gt; props = new HashMap&lt;String, Object&gt;();
 *       <span style="font-weight:bold">props.put(JSONJAXBContext.JSON_NOTATION, JSONJAXBContext.JSONNotation.MAPPED);
 *       props.put(JSONJAXBContext.JSON_ROOT_UNWRAPPING, Boolean.TRUE);
 *       props.put(JSONJAXBContext.JSON_NON_STRINGS, new HashSet&lt;String&gt;(1){{add("number");}});</span>
 *       this.types = new HashSet(Arrays.asList(cTypes));
 *       this.context = new <span style="font-weight:bold">JSONJAXBContext</span>(cTypes, <span style="font-weight:bold">props</span>);
 *   }
 *
 *   <span style="font-weight:bold">public JAXBContext getContext(Class&lt;?&gt; objectType)</span> {
 *       return (types.contains(objectType)) ? <span style="font-weight:bold">context</span> : null;
 *   }
 * }
 * </pre></blockquote>
 *
 * <p>For a complete set of supported properties, please see {@link com.sun.jersey.api.json.JSONJAXBContext}.
 */
package com.sun.jersey.api.json;
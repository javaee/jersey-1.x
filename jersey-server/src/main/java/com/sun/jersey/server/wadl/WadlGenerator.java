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

package com.sun.jersey.server.wadl;

import com.sun.jersey.api.model.AbstractMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRegistry;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.Parameter;
import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.RepresentationType;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;


/**
 * A WadlGenerator creates artifacts related to wadl. This is designed as an interface,
 * so that several implementations can decorate existing ones. One decorator could e.g. add
 * references to definitions within some xsd for existing representations.<br>
 * Created on: Jun 16, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public interface WadlGenerator {
    
    /**
     * Sets the delegate that is decorated by this wadl generator. Is invoked directly after
     * this generator is instantiated before {@link #init()} or any setter method is invoked.
     * @param delegate the wadl generator to decorate
     */
    void setWadlGeneratorDelegate( WadlGenerator delegate );
    
    /**
     * Invoked before all methods related to wadl-building are invoked. This method is used in a
     * decorator like manner, and therefore has to invoke <code>this.delegate.init()</code>.
     * @throws Exception
     */
    void init() throws Exception;
    
    /**
     * The jaxb context path that is used when the generated wadl application is marshalled
     * to a file.<br/>
     * This method is used in a decorator like manner.<br/>
     * The result return the path (or a colon-separated list of package names) containing
     * jaxb-beans that are added to wadl elements by this WadlGenerator, additionally to
     * the context path of the decorated WadlGenerator (set by {@link #setWadlGeneratorDelegate(WadlGenerator)}.<br/>
     * If you do not use custom jaxb beans, then simply return <code>_delegate.getRequiredJaxbContextPath()</code>,
     * otherwise return the delegate's {@link #getRequiredJaxbContextPath()} together with
     * your required context path (separated by a colon):<br/>
     * <pre><code>_delegate.getRequiredJaxbContextPath() == null
            ? ${yourContextPath}
            : _delegate.getRequiredJaxbContextPath() + ":" + ${yourContextPath};</code></pre>
     * 
     * If you add the path for your custom jaxb beans, don't forget to add an
     * ObjectFactory (annotated with {@link XmlRegistry}) to this package.
     * @return simply the {@link #getRequiredJaxbContextPath()} of the delegate or the
     *  {@link #getRequiredJaxbContextPath()} + ":" + ${yourContextPath}.
     */
    String getRequiredJaxbContextPath();
    
    // ================  methods for building the wadl application =============

    public Application createApplication();

    public Resources createResources();
    
    public Resource createResource(AbstractResource r,
            String path);

    public com.sun.research.ws.wadl.Method createMethod(AbstractResource r, 
            AbstractResourceMethod m);

    public Request createRequest(AbstractResource r, 
            AbstractResourceMethod m);

    public RepresentationType createRequestRepresentation(AbstractResource r, 
            AbstractResourceMethod m, 
            MediaType mediaType);

    public Response createResponse(AbstractResource r,
            AbstractResourceMethod m);    
    
    public Param createParam(AbstractResource r, 
            AbstractMethod m,
            Parameter p);
}
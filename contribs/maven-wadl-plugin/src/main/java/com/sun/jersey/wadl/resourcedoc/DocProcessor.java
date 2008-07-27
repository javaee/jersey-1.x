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
package com.sun.jersey.wadl.resourcedoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.jersey.impl.wadl.WadlGenerator;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ClassDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.MethodDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ParamDocType;

/**
 * A doc processor is handed over javadoc elements so that it can turn this into
 * resource doc elements, even self defined.<br>
 * Created on: Jul 5, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public interface DocProcessor {
    
    Class<?>[] getRequiredJaxbContextClasses();

    /**
     * specify which of your elements you want to be handled as CDATA.
     * The use of the '^' between the namespaceURI and the localname
     * seems to be an implementation detail of the xerces code.
     * When processing xml that doesn't use namespaces, simply omit the
     * namespace prefix as shown in the third CDataElement below.
     * 
     * @return an Array of element descriptors or <code>null</code>
     * 
     */
    String[] getCDataElements();

    void processClassDoc( ClassDoc classDoc, ClassDocType classDocType );

    /**
     * Process the provided methodDoc and add your custom information to the methodDocType.<br/>
     * Use e.g. {@link MethodDocType#getAny()} to store custom elements.
     * @param methodDoc the {@link MethodDoc} representing the docs of your method.
     * @param methodDocType the related {@link MethodDocType} that will lated be processed by the {@link WadlGenerator}s.
     * @author Martin Grotzke
     */
    void processMethodDoc( MethodDoc methodDoc, MethodDocType methodDocType );

    void processParamTag( ParamTag paramTag, Parameter parameter, ParamDocType paramDocType );

}

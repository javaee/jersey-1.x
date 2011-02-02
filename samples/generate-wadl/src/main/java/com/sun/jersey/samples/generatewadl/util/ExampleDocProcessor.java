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
package com.sun.jersey.samples.generatewadl.util;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Tag;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ClassDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.MethodDocType;
import com.sun.jersey.server.wadl.generators.resourcedoc.model.ParamDocType;
import com.sun.jersey.wadl.resourcedoc.DocProcessor;

/**
 * This {@link DocProcessor} shows how you can process your custom tags and
 * add them to the generated resourcedoc.xml.<br>
 * Created on: Jul 20, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class ExampleDocProcessor implements DocProcessor {

    protected static final String EXAMPLE_TAG = "exampletag";
    
    public Class<?>[] getRequiredJaxbContextClasses() {
        return new Class[] { MyNamedValueType.class };
    }

    public String[] getCDataElements() {
        return null;
    }
    
    public void processMethodDoc( MethodDoc methodDoc, MethodDocType methodDocType ) {
        final String tagName = "@example.tag";
        final Tag exampleTag = getTag( methodDoc, tagName );
        if ( exampleTag != null ) {
            final MyNamedValueType namedValueType = new MyNamedValueType();
            namedValueType.setName( EXAMPLE_TAG );
            namedValueType.setValue( exampleTag.text() );
            methodDocType.getAny().add( namedValueType );
        }
    }

    private Tag getTag( MethodDoc methodDoc, final String tagName ) {
        for ( Tag tag : methodDoc.tags() ) {
            if ( tagName.equals( tag.name() ) ) {
                return tag;
            }
        }
        return null;
    }
    
    public void processClassDoc( ClassDoc arg0, ClassDocType arg1 ) {
    }
    
    public void processParamTag( ParamTag arg0, Parameter arg1,
            ParamDocType arg2 ) {
    }

}

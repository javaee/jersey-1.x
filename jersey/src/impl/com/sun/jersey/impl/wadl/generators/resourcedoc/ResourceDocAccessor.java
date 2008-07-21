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
package com.sun.jersey.impl.wadl.generators.resourcedoc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.QueryParam;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.model.Parameter.Source;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.AnnotationDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ClassDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.MethodDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.NamedValueType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ParamDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.RepresentationDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ResourceDocType;
import com.sun.jersey.impl.wadl.generators.resourcedoc.model.ResponseDocType;

/**
 * A class providing access to information stored in a {@link ResourceDocType}.<br>
 * Created on: Jun 16, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class ResourceDocAccessor {

    private ResourceDocType _resourceDoc;

    public ResourceDocAccessor(ResourceDocType resourceDoc) {
        _resourceDoc = resourceDoc;
    }

    public ClassDocType getClassDoc( Class<?> resourceClass ) {
        for ( ClassDocType classDocType : _resourceDoc.getDocs() ) {
            if( resourceClass.getName().equals( classDocType.getClassName() ) ) {
                return classDocType;
            }
        }
        return null;
    }

    public MethodDocType getMethodDoc( Class<?> resourceClass, Method method ) {
        final ClassDocType classDoc = getClassDoc( resourceClass );
        if ( classDoc != null ) {
            for ( MethodDocType methodDocType : classDoc.getMethodDocs() ) {
                if ( method.getName().equals( methodDocType.getMethodName() ) ) {
                    return methodDocType;
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param resourceClass
     * @param method
     * @param p
     * @return
     * @author Martin Grotzke
     */
    public ParamDocType getParamDoc( Class<?> resourceClass, Method method,
            Parameter p ) {
        final MethodDocType methodDoc = getMethodDoc( resourceClass, method );
        if ( methodDoc != null ) {
            for ( ParamDocType paramDocType : methodDoc.getParamDocs() ) {
                for ( AnnotationDocType annotationDocType : paramDocType.getAnnotationDocs() ) {
                    final Class<? extends Annotation> annotationType = ParameterAnnotationMapping.getAnnotationTypeBySource( p.getSource() );
                    if ( annotationType != null ) {
                        final String sourceName = getSourceName( p, paramDocType, annotationDocType );
                        if ( sourceName != null && sourceName.equals( p.getSourceName() ) ) {
                            return paramDocType;
                        }
                    }
                }
            }
        }
        return null;
    }

    public RepresentationDocType getRequestRepresentation( Class<?> resourceClass, Method method, String mediaType ) {
        if ( mediaType == null ) {
            return null;
        }
        final MethodDocType methodDoc = getMethodDoc( resourceClass, method );
        return methodDoc != null
            && methodDoc.getRequestDoc() != null
            && methodDoc.getRequestDoc().getRepresentationDoc() != null
            // && mediaType.equals( methodDoc.getRequestDoc().getRepresentationDoc().getMediaType() )
                ? methodDoc.getRequestDoc().getRepresentationDoc() : null;
    }

    public ResponseDocType getResponse( Class<?> resourceClass, Method method ) {
        final MethodDocType methodDoc = getMethodDoc( resourceClass, method );
        return methodDoc != null && methodDoc.getResponseDoc() != null
                ? methodDoc.getResponseDoc() : null;
    }

    private String getSourceName( Parameter p, ParamDocType paramDocType,
            AnnotationDocType annotationDocType ) {
        if ( annotationDocType.hasAttributeDocs() ) {
            for ( NamedValueType namedValueType : annotationDocType.getAttributeDocs() ) {
                /* the value of the "value"-attribute is the param.sourceName...
                 */
                if ( "value".equals( namedValueType.getName() ) ) {
                    return namedValueType.getValue();
                }
            }
        }
        return null;
    }
    
    enum ParameterAnnotationMapping {
        
        QUERY_PARAM( QueryParam.class, Source.QUERY );
        
        private final Class<? extends Annotation> _jaxRsParamAnnotationType;
        private final Parameter.Source _parameterSource;
        
        private ParameterAnnotationMapping(
                Class<? extends Annotation> jaxRsParamAnnotationType,
                Source parameterSource) {
            _jaxRsParamAnnotationType = jaxRsParamAnnotationType;
            _parameterSource = parameterSource;
        }

        public static Class<? extends Annotation> getAnnotationTypeBySource( Source source ) {
            if ( source == null ) {
                return null;
            }
            for( ParameterAnnotationMapping mapping : values() ) {
                if( source == mapping._parameterSource ) {
                    return mapping._jaxRsParamAnnotationType;
                }
            }
            return null;
        }
        
        
        
    }

}

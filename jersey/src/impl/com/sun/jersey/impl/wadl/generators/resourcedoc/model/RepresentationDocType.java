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
package com.sun.jersey.impl.wadl.generators.resourcedoc.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * The documentation type for representations.<br>
 * Created on: Jun 16, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "representationDoc", propOrder = {

})
public class RepresentationDocType {

    @XmlAttribute
    private QName element;
    private String example;
    @XmlAttribute
    private Long status;
    @XmlAttribute
    private String mediaType;
    private String doc;
    
    /**
     * @return the element
     * @author Martin Grotzke
     */
    public QName getElement() {
        return element;
    }
    /**
     * @param element the element to set
     * @author Martin Grotzke
     */
    public void setElement( QName element ) {
        this.element = element;
    }
    /**
     * @return the example
     * @author Martin Grotzke
     */
    public String getExample() {
        return example;
    }
    /**
     * @param example the example to set
     * @author Martin Grotzke
     */
    public void setExample( String example ) {
        this.example = example;
    }
    /**
     * @return the status
     * @author Martin Grotzke
     */
    public Long getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     * @author Martin Grotzke
     */
    public void setStatus( Long status ) {
        this.status = status;
    }
    /**
     * @return the mediaType
     * @author Martin Grotzke
     */
    public String getMediaType() {
        return mediaType;
    }
    /**
     * @param mediaType the mediaType to set
     * @author Martin Grotzke
     */
    public void setMediaType( String mediaType ) {
        this.mediaType = mediaType;
    }
    /**
     * @return the doc
     * @author Martin Grotzke
     */
    public String getDoc() {
        return doc;
    }
    /**
     * @param doc the doc to set
     * @author Martin Grotzke
     */
    public void setDoc( String doc ) {
        this.doc = doc;
    }
    
}

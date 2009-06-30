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

package com.sun.jersey.api.json;

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A JSON marshaller responsible for serializing Java content trees, defined
 * by JAXB, to JSON data.
 * 
 * @author Jakub.Podlesak@Sun.COM, Paul.Sandoz@Sun.COM
 */
public interface JSONMarshaller {

    /**
     * Marshall the content tree rooted at <code>jaxbElement</code> into an
     * output stream. The content tree may be an instance of a class that is
     * mapped to a XML root element (for example, annotated with
     * {@link XmlRootElement}) or an instance of {@link JAXBElement}.
     * <p>
     * The UTF-8 character encoding scheme will be used to encode the characters
     * of the JSON data.
     * 
     * @param jaxbElement the root of the content tree to be marshalled.
     * @param os the JSON will be added to this stream.
     * @throws JAXBException if any unexpected problem occurs during the
     *         marshalling.
     * @throws MarshalException if the <code>JSONMarshaller</code> is unable to
     *         marshal <code>jaxbElement</code> (or any object reachable from obj)
     * @throws IllegalArgumentException if any of the method parameters are null.
     *
     */
    void marshallToJSON(Object jaxbElement, OutputStream os) throws JAXBException;

    /**
     * Marshall the content tree rooted at <code>jaxbElement</code> into an
     * output stream. The content tree may be an instance of a class that is
     * mapped to a XML root element (for example, annotated with
     * {@link XmlRootElement}) or an instance of {@link JAXBElement}.
     * <p>
     * The character encoding scheme of the <code>writer</code> will be used to
     * encode the characters of the JSON data.
     *
     * @param jaxbElement the root of the content tree to be marshalled.
     * @param writer the JSON will be added to this writer.
     * @throws JAXBException if any unexpected problem occurs during the
     *         marshalling.
     * @throws MarshalException if the <code>JSONMarshaller</code> is unable to
     *         marshal <code>jaxbElement</code> (or any object reachable from obj)
     * @throws IllegalArgumentException If any of the method parameters are null.
     */
    void marshallToJSON(Object jaxbElement, Writer writer) throws JAXBException;
}
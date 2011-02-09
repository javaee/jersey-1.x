/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.core.impl.provider.entity;

import com.sun.jersey.core.provider.jaxb.AbstractRootElementProvider;
import com.sun.jersey.spi.inject.Injectable;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class XMLRootElementProvider extends AbstractRootElementProvider {

    // Delay construction of factory
    private final Injectable<SAXParserFactory> spf;

    XMLRootElementProvider(Injectable<SAXParserFactory> spf, Providers ps) {
        super(ps);

        this.spf = spf;
    }

    XMLRootElementProvider(Injectable<SAXParserFactory> spf, Providers ps, MediaType mt) {
        super(ps, mt);

        this.spf = spf;
    }

    @Produces("application/xml")
    @Consumes("application/xml")
    public static final class App extends XMLRootElementProvider {

        public App(@Context Injectable<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps, MediaType.APPLICATION_XML_TYPE);
        }
    }

    @Produces("text/xml")
    @Consumes("text/xml")
    public static final class Text extends XMLRootElementProvider {

        public Text(@Context Injectable<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps, MediaType.TEXT_XML_TYPE);
        }
    }

    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends XMLRootElementProvider {

        public General(@Context Injectable<SAXParserFactory> spf, @Context Providers ps) {
            super(spf, ps);
        }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+xml");
        }
    }

    @Override
    protected Object readFrom(Class<Object> type, MediaType mediaType,
            Unmarshaller u, InputStream entityStream)
            throws JAXBException {
        final SAXSource s = getSAXSource(spf.getValue(), entityStream);
        if (type.isAnnotationPresent(XmlRootElement.class)) {
            return u.unmarshal(s);
        } else {
            return u.unmarshal(s, type).getValue();
        }
    }
}

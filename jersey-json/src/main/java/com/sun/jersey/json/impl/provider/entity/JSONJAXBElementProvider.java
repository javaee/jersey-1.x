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

package com.sun.jersey.json.impl.provider.entity;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONUnmarshaller;
import com.sun.jersey.core.provider.jaxb.AbstractJAXBElementProvider;
import com.sun.jersey.json.impl.JSONHelper;
import com.sun.jersey.json.impl.Stax2JsonFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author japod
 */
public class JSONJAXBElementProvider extends AbstractJAXBElementProvider {
    
    JSONJAXBElementProvider(Providers ps) {
        super(ps);
    }

    JSONJAXBElementProvider(Providers ps, MediaType mt) {
        super(ps, mt);
    }

    @Produces("application/json")
    @Consumes("application/json")
    public static final class App extends JSONJAXBElementProvider {
        public App(@Context Providers ps) { super(ps , MediaType.APPLICATION_JSON_TYPE); }
    }

    @Produces("*/*")
    @Consumes("*/*")
    public static final class General extends JSONJAXBElementProvider {
        public General(@Context Providers ps) { super(ps); }

        @Override
        protected boolean isSupported(MediaType m) {
            return m.getSubtype().endsWith("+json");
        }
    }

    protected final JAXBElement<?> readFrom(Class<?> type, MediaType mediaType,
            Unmarshaller u, InputStream entityStream)
            throws JAXBException, IOException {

        return JSONJAXBContext.getJSONUnmarshaller(u).unmarshalJAXBElementFromJSON(entityStream, type);
    }
    
    protected final void writeTo(JAXBElement<?> t, MediaType mediaType, Charset c,
            Marshaller m, OutputStream entityStream)
            throws JAXBException, IOException {

        JSONJAXBContext.getJSONMarshaller(m).
                marshallToJSON(t, new OutputStreamWriter(entityStream, c));
    }
}
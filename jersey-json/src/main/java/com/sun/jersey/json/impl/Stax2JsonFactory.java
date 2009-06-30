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
package com.sun.jersey.json.impl;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.json.impl.writer.*;
import com.sun.jersey.json.impl.reader.Jackson2StaxReader;
import com.sun.jersey.json.impl.reader.JacksonRootAddingParser;
import com.sun.jersey.json.impl.reader.JsonXmlStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamReader;
import org.codehaus.jettison.badgerfish.BadgerFishXMLStreamWriter;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

/**
 *
 * @author japod
 */
public class Stax2JsonFactory {

    private Stax2JsonFactory() {
    }

    public static XMLStreamWriter createWriter(Writer writer, JSONConfiguration config) throws IOException {
        return createWriter(writer, config, false);
    }

    public static XMLStreamWriter createWriter(Writer writer, JSONConfiguration config, boolean writingList) throws IOException {
        switch (config.getNotation()) {
            case NATURAL:
                final JsonGenerator rawGenerator = new JsonFactory().createJsonGenerator(writer);
                final JsonGenerator bodyGenerator = writingList ? JacksonArrayWrapperGenerator.createArrayWrapperGenerator(rawGenerator) : rawGenerator;
                if (config.isRootUnwrapping()) {
                    return new Stax2JacksonWriter(JacksonRootStrippingGenerator.createRootStrippingGenerator(bodyGenerator));
                } else {
                    return new Stax2JacksonWriter(bodyGenerator);
                }
            case MAPPED:
                return JsonXmlStreamWriter.createWriter(writer, config);
            case BADGERFISH:
                return new BadgerFishXMLStreamWriter(writer);
            case MAPPED_JETTISON:
                Configuration jmConfig;
                if (null == config.getXml2JsonNs()) {
                    jmConfig = new Configuration();
                } else {
                    jmConfig = new Configuration(config.getXml2JsonNs());
                }
                return new MappedXMLStreamWriter(new MappedNamespaceConvention(jmConfig), writer);
            default:
                return null;
        }
    }

    public static XMLStreamReader createReader(Reader reader, JSONConfiguration config, String rootName) {
        return createReader(reader, config, rootName, false);
    }

    public static XMLStreamReader createReader(Reader reader, JSONConfiguration config, String rootName, boolean readingList) {
        switch (config.getNotation()) {
            case NATURAL:
                try {
                    final JsonParser rawParser = new JsonFactory().createJsonParser(reader);
                    final JsonParser nonListParser = config.isRootUnwrapping() ? JacksonRootAddingParser.createRootAddingParser(rawParser, rootName) : rawParser;
                    if (!readingList) {
                        return new Jackson2StaxReader(nonListParser);
                    } else {
                        return new Jackson2StaxReader(JacksonRootAddingParser.createRootAddingParser(nonListParser, "jsonArrayRootElement"));
                    }
                } catch (Exception ex) {
                    Logger.getLogger(JSONUnmarshallerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case MAPPED:
                try {
                    return new JsonXmlStreamReader(reader, rootName, config);
                } catch (IOException ex) {
                    Logger.getLogger(JSONUnmarshallerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case MAPPED_JETTISON:
                try {
                    Configuration jmConfig;
                    if (null == config.getXml2JsonNs()) {
                        jmConfig = new Configuration();
                    } else {
                        jmConfig = new Configuration(config.getXml2JsonNs());
                    }
                    return new MappedXMLStreamReader(
                            new JSONObject(new JSONTokener(readFromAsString(reader))),
                            new MappedNamespaceConvention(jmConfig));
                } catch (Exception ex) {
                    Logger.getLogger(JSONUnmarshallerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case BADGERFISH:
                try {
                    return new BadgerFishXMLStreamReader(new JSONObject(new JSONTokener(readFromAsString(reader))));
                } catch (Exception ex) {
                    Logger.getLogger(JSONUnmarshallerImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
        return null;
    }

    private static String readFromAsString(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] c = new char[1024];
        int l;
        while ((l = reader.read(c)) != -1) {
            sb.append(c, 0, l);
        }
        return sb.toString();
    }
}

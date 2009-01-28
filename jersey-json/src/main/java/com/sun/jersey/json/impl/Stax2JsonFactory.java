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
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.json.impl.reader.Jackson2StaxReader;
import com.sun.jersey.json.impl.reader.JacksonRootAddingParser;
import com.sun.jersey.json.impl.reader.JsonXmlStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.codehaus.jackson.JsonFactory;
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
                if (writingList) {
                    return new Stax2JacksonWriter(JacksonArrayWrapperGenerator.createArrayWrapperGenerator(JacksonRootStrippingGenerator.createRootStrippingGenerator(new JsonFactory().createJsonGenerator(writer))));
                } else {
                    return new Stax2JacksonWriter(JacksonRootStrippingGenerator.createRootStrippingGenerator(new JsonFactory().createJsonGenerator(writer)));
                }
            case MAPPED:
                return JsonXmlStreamWriter.createWriter(writer, config.isRootUnwrapping(), config.getArrays(), config.getNonStrings(), config.getAttrsAsElems());
            case BADGERFISH:
                return new BadgerFishXMLStreamWriter(writer);
            case MAPPED_JETTISON:
                Configuration jmConfig;
                if (null == config.getJsonXml2JsonNs()) {
                    jmConfig = new Configuration();
                } else {
                    jmConfig = new Configuration(config.getJsonXml2JsonNs());
                }
                return new MappedXMLStreamWriter(
                        new MappedNamespaceConvention(jmConfig), writer);
            default:
                return null;
        }
    }

    public static XMLStreamReader createReader(Reader reader, JSONConfiguration config, String rootName) {
        switch (config.getNotation()) {
            case NATURAL:
                try {
                    return new Jackson2StaxReader(JacksonRootAddingParser.createRootAddingParser(new JsonFactory().createJsonParser(reader), rootName));
                } catch (Exception ex) {
                    Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case MAPPED:
                try {
                    return new JsonXmlStreamReader(reader, rootName, config.getAttrsAsElems());
                } catch (IOException ex) {
                    Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case MAPPED_JETTISON:
                try {
                    Configuration jmConfig;
                    if (null == config.getJsonXml2JsonNs()) {
                        jmConfig = new Configuration();
                    } else {
                        jmConfig = new Configuration(config.getJsonXml2JsonNs());
                    }
                    return new MappedXMLStreamReader(
                            new JSONObject(new JSONTokener(readFromAsString(reader))),
                            new MappedNamespaceConvention(jmConfig));
                } catch (Exception ex) {
                    Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case BADGERFISH:
                try {
                    return new BadgerFishXMLStreamReader(new JSONObject(new JSONTokener(readFromAsString(reader))));
                } catch (Exception ex) {
                    Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
        }
        return null;
    }

//    @Deprecated
//    public static XMLStreamWriter createWriter(JSONJAXBContext.JSONNotation notation, Writer writer, boolean stripRoot, Collection<String> arrays, Collection<String> nonStrings, Collection<String> attrAsElemName, Map<String, String> xml2jsonNamespace) throws IOException {
//        switch (notation) {
//            case NATURAL:
//                return new Stax2JacksonWriter(JacksonRootStrippingGenerator.createRootStrippingGenerator(new JsonFactory().createJsonGenerator(writer)));
//            case MAPPED:
//                return JsonXmlStreamWriter.createWriter(writer, stripRoot, arrays, nonStrings, attrAsElemName);
//            case BADGERFISH:
//                return new BadgerFishXMLStreamWriter(writer);
//            case MAPPED_JETTISON:
//                Configuration jmConfig;
//                if (null == xml2jsonNamespace) {
//                    jmConfig = new Configuration();
//                } else {
//                    jmConfig = new Configuration(xml2jsonNamespace);
//                }
//                return new MappedXMLStreamWriter(
//                        new MappedNamespaceConvention(jmConfig), writer);
//
//            default:
//                return null;
//        }
//    }
//
//    @Deprecated
//    public static XMLStreamReader createReader(JSONJAXBContext.JSONNotation notation, Reader reader, String rootName, Collection<String> attrAsElemName, Map<String, String> xml2jsonNamespace) {
//        switch (notation) {
//            case NATURAL:
//                try {
//                    return new Jackson2StaxReader(JacksonRootAddingParser.createRootAddingParser(new JsonFactory().createJsonParser(reader), rootName));
//                } catch (Exception ex) {
//                    Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//            case MAPPED:
//                try {
//                    return new JsonXmlStreamReader(reader, rootName, attrAsElemName);
//                } catch (IOException ex) {
//                    Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//            case MAPPED_JETTISON:
//                try {
//                    Configuration jmConfig;
//                    if (null == xml2jsonNamespace) {
//                        jmConfig = new Configuration();
//                    } else {
//                        jmConfig = new Configuration(xml2jsonNamespace);
//                    }
//                    return new MappedXMLStreamReader(
//                            new JSONObject(new JSONTokener(readFromAsString(reader))),
//                            new MappedNamespaceConvention(jmConfig));
//                } catch (Exception ex) {
//                    Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//            case BADGERFISH:
//                try {
//                    return new BadgerFishXMLStreamReader(new JSONObject(new JSONTokener(readFromAsString(reader))));
//                } catch (Exception ex) {
//                    Logger.getLogger(JSONUnmarshaller.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                break;
//        }
//        return null;
//    }
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

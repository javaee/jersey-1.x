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

package com.sun.jersey.atom.rome.impl.provider.entity;

import com.sun.jersey.atom.rome.impl.ImplMessages;
import com.sun.jersey.core.provider.AbstractMessageReaderWriterProvider;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.WireFeedOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class AtomEntryProvider extends AbstractMessageReaderWriterProvider<Entry> {
    private static final String FEED_TYPE = "atom_1.0";
    
    public AtomEntryProvider() {
        Class<?> c = Entry.class;        
    }
    
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return type == Entry.class;
    }

    public Entry readFrom(
            Class<Entry> type, 
            Type genericType, 
            Annotation annotations[],
            MediaType mediaType, 
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        try {
            return parseEntry(entityStream);
        } catch (FeedException cause) {
            IOException effect = new IOException(ImplMessages.ERROR_CREATING_ATOM(type));
            effect.initCause(cause);
            throw effect;
        } catch (JDOMException cause2) {
            IOException effect = new IOException(ImplMessages.ERROR_CREATING_ATOM(type));
            effect.initCause(cause2);
            throw effect;
        }
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return type == Entry.class;        
    }
    
    public void writeTo(
            Entry t, 
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            serializeEntry(t, entityStream);
        } catch (FeedException cause) {
            IOException effect = new IOException(ImplMessages.ERROR_MARSHALLING_ATOM(t.getClass()));
            effect.initCause(cause);
            throw effect;
        }
    }
    
    /**
     * Parse entry from InputStream.
     */
    private static Entry parseEntry(InputStream in) 
    throws JDOMException, IOException, IllegalArgumentException, FeedException {
        // Parse entry into JDOM tree
        SAXBuilder builder = new SAXBuilder();
        Document entryDoc = builder.build(in);
        Element fetchedEntryElement = entryDoc.getRootElement();
        fetchedEntryElement.detach();
        
        // Put entry into a JDOM document with 'feed' root so that Rome can handle it
        Feed feed = new Feed();
        feed.setFeedType(FEED_TYPE);
        WireFeedOutput wireFeedOutput = new WireFeedOutput();
        Document feedDoc = wireFeedOutput.outputJDom(feed);
        feedDoc.getRootElement().addContent(fetchedEntryElement);
                
        WireFeedInput input = new WireFeedInput();
        Feed parsedFeed = (Feed)input.build(feedDoc);
        return (Entry)parsedFeed.getEntries().get(0);
    }
    
     /**
     * Serialize an entry to an OutputStream.
     * 
     * @param entry the entry to serialize
     * @param out the output stream to which the entry will be written
     * @throws java.lang.IllegalArgumentException if the entry is malformed
     * @throws com.sun.syndication.io.FeedException if an error is raised by the ROME library
     * @throws java.io.IOException if an IO error occurs
     */
    @SuppressWarnings("unchecked")
    private static void serializeEntry(Entry entry, OutputStream out) 
    throws IllegalArgumentException, FeedException, IOException {
        // Build a feed containing only the entry
        List entries = new ArrayList();
        entries.add(entry);
        Feed feed1 = new Feed();
        feed1.setFeedType( FEED_TYPE);
        feed1.setEntries(entries);
        
        // Use Rome to output feed as a JDOM document
        WireFeedOutput wireFeedOutput = new WireFeedOutput();
        Document feedDoc = wireFeedOutput.outputJDom(feed1);
        
        // Grab entry element from feed and get JDOM to serialize it
        Element entryElement= (Element)feedDoc.getRootElement().getChildren().get(0);
                
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        
        outputter.output(entryElement, out);
    }
}

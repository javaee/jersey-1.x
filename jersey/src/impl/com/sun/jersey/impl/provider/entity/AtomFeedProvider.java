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

package com.sun.jersey.impl.provider.entity;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.WireFeedOutput;
import com.sun.jersey.impl.ImplMessages;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class AtomFeedProvider extends AbstractMessageReaderWriterProvider<Feed> {
    private static final String FEED_TYPE = "atom_1.0";
    
    public AtomFeedProvider() {
        Class<?> c = Feed.class;        
    }
    
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
        return type == Feed.class;
    }

    public Feed readFrom(
            Class<Feed> type, 
            Type genericType, 
            Annotation annotations[],
            MediaType mediaType, 
            MultivaluedMap<String, String> httpHeaders, 
            InputStream entityStream) throws IOException {
        try {
            WireFeedInput input = new WireFeedInput();                      
            WireFeed wireFeed = input.build(new InputStreamReader(entityStream));    
            if (!(wireFeed instanceof Feed)) {
                throw new IOException(ImplMessages.ERROR_NOT_ATOM_FEED(type));
            }
            return (Feed)wireFeed;
        } catch (FeedException cause) {
            IOException effect = new IOException(ImplMessages.ERROR_MARSHALLING_ATOM(type));
            effect.initCause(cause);
            throw effect;
        }
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
        return type == Feed.class;
    }
    
    public void writeTo(
            Feed t, 
            Class<?> type, 
            Type genericType, 
            Annotation annotations[], 
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            t.setFeedType(FEED_TYPE);
            WireFeedOutput wireFeedOutput = new WireFeedOutput();
            Document feedDoc = wireFeedOutput.outputJDom(t);

            XMLOutputter outputter = new XMLOutputter();
            outputter.setFormat(Format.getPrettyFormat());
            outputter.output(feedDoc, entityStream);          
        } catch ( FeedException cause) {
            IOException effect = new IOException(ImplMessages.ERROR_MARSHALLING_ATOM(t.getClass()));
            effect.initCause(cause);
            throw effect;
        }
    }    
}
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.provider.entity;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.io.WireFeedOutput;
import com.sun.ws.rest.impl.ImplMessages;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.ws.rs.core.MultivaluedMap;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class AtomFeedProvider extends AbstractTypeEntityProvider<Feed> {
    private static final String FEED_TYPE = "atom_1.0";
    
    public boolean supports(Class type) {
        return type == Feed.class;
    }

    public Feed readFrom(Class<Feed> type, 
            String mediaType, MultivaluedMap<String, String> headers, InputStream entityStream) throws IOException {
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

    public void writeTo(Feed t, 
            MultivaluedMap<String, Object> headers, OutputStream entityStream) throws IOException {
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

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

package com.sun.ws.rest.impl.entity;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.ws.rest.impl.provider.entity.AtomEntryProvider;
import com.sun.ws.rest.impl.provider.entity.AtomFeedProvider;
import java.io.InputStream;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class AtomStreamingTest extends AbstractStreamingTester {
    
    public AtomStreamingTest(String testName) {
        super(testName);
    }
    
    public void testFeed() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("feed.xml");
        AtomFeedProvider afp = new AtomFeedProvider();
        Feed f = afp.readFrom(Feed.class, null, null, in);
        roundTrip(Feed.class, f);
    }
    
    public void testEntry() throws Exception {
        InputStream in = this.getClass().getResourceAsStream("entry.xml");        
        AtomEntryProvider afp = new AtomEntryProvider();
        Entry e = afp.readFrom(Entry.class, null, null, in);
        roundTrip(Entry.class, e);
    }
}
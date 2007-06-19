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

package com.sun.ws.rest.impl.http.header.reader;

import java.text.ParseException;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class HttpHeaderListAdapter extends HttpHeaderReader {
    private HttpHeaderReader reader;
            
    boolean isTerminated;
        
    public HttpHeaderListAdapter(HttpHeaderReader reader) {
        this.reader = reader;
    }
    
    public void reset() {
        isTerminated = false;
    }

    
    public boolean hasNext() {
        if (isTerminated)
            return false;
        
        if (reader.hasNext()) {
            if (reader.hasNextSeparator(',', true)) {
                isTerminated = true;
                return false;
            } else
                return true;
        }
        
        return false;
    }

    public boolean hasNextSeparator(char separator, boolean skipWhiteSpace) {
        if (isTerminated)
            return false;
        
        if (reader.hasNextSeparator(',', skipWhiteSpace)) {
            isTerminated = true;
            return false;
        } else
            return reader.hasNextSeparator(separator, skipWhiteSpace);
    }
    
    public Event next() throws ParseException {
        return next(true);
    }

    public HttpHeaderReader.Event next(boolean skipWhiteSpace) throws ParseException {
        if (isTerminated)
            throw new ParseException("End of header", getIndex());
        
        if (reader.hasNextSeparator(',', skipWhiteSpace)) {
            isTerminated = true;
            throw new ParseException("End of header", getIndex());
        }
        
        return reader.next(skipWhiteSpace);
    }

    public HttpHeaderReader.Event getEvent() {
        return reader.getEvent();
    }

    public String getEventValue() {
        return reader.getEventValue();
    }

    public String getRemainder() {
        return reader.getRemainder();
    }    

    public int getIndex() {
        return reader.getIndex();
    }
}

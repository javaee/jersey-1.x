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

package com.sun.ws.rest.impl.provider.header;

import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReader;
import com.sun.ws.rest.impl.http.header.reader.HttpHeaderReaderImpl;
import com.sun.ws.rest.impl.http.header.writer.WriterUtil;
import com.sun.jersey.spi.HeaderDelegateProvider;
import java.text.ParseException;
import javax.ws.rs.core.EntityTag;

/**
 *
 * @author Marc.Hadley@Sun.Com
 */
public class EntityTagProvider implements HeaderDelegateProvider<EntityTag> {
    
    public boolean supports(Class<?> type) {
        return type == EntityTag.class;
    }

    public String toString(EntityTag header) {
        StringBuilder b = new StringBuilder();
        if (header.isWeak())
            b.append("W/");
        WriterUtil.appendQuoted(b,header.getValue());
        return b.toString();
    }

    public EntityTag fromString(String header) {
        boolean weak = false;
        if (header.startsWith("W/")) {
            header = header.substring(2);
            weak = true;
        }
        HttpHeaderReader reader = new HttpHeaderReaderImpl(header);
        try {
            EntityTag eTag = new EntityTag(reader.nextQuotedString(),weak);
            return eTag;
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }    
}

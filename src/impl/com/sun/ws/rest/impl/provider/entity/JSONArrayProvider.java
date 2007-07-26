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

import com.sun.ws.rest.impl.ImplMessages;
import com.sun.ws.rest.impl.util.ThrowHelper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.ws.rs.core.MultivaluedMap;
import org.json.JSONArray;
import org.json.JSONException;

/**
 *
 * @author japod
 */
public class JSONArrayProvider  extends AbstractTypeEntityProvider<JSONArray>{
    
    
    public void writeTo(JSONArray jsonArray, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(entityStream);
            jsonArray.write(writer);
            writer.write("\n");
            writer.flush();
        } catch (JSONException je) {
            throw ThrowHelper.withInitCause(je, new IOException(ImplMessages.ERROR_WRITING_JSON_ARRAY()));
        }
    }
    
    public boolean supports(Class<?> type) {
        return type == JSONArray.class;
    }
    
    public JSONArray readFrom(Class<JSONArray> o, String mediaType, MultivaluedMap<String, String> headers, InputStream is) throws IOException {
        try {
            return new JSONArray(readFromAsString(is));
        } catch (JSONException je) {
            throw ThrowHelper.withInitCause(je, new IOException(ImplMessages.ERROR_PARSING_JSON_ARRAY()));
        }
    }
}

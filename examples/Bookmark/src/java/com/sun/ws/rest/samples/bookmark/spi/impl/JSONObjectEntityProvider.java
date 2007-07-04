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

package com.sun.ws.rest.samples.bookmark.spi.impl;


import com.sun.ws.rest.impl.provider.entity.AbstractTypeEntityProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.ws.rs.core.MultivaluedMap;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author japod
 */
public class JSONObjectEntityProvider  extends AbstractTypeEntityProvider<JSONObject>{
    
    /**
     * Creates a new instance of JSONObjectEntityProvider
     */
    public JSONObjectEntityProvider() {
    }
    
    public void writeTo(JSONObject jsonObject, MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(entityStream);
            jsonObject.write(writer);
            writer.write("\n");
            writer.flush();
        } catch (JSONException je) {
            throw (IOException)(new IOException("error writing json object")).initCause(je);
        }
    }
    
    public boolean supports(Class<?> type) {
        return type == JSONObject.class;
    }
    
    public JSONObject readFrom(Class<JSONObject> o, String mediaType, MultivaluedMap<String, String> headers, InputStream is) throws IOException {
        try {
            return new JSONObject(readFromAsString(is));
        } catch (JSONException je) {
            throw (IOException)(new IOException("error parsing json object")).initCause(je);
        }
    }
}

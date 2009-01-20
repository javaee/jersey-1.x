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

package com.sun.jersey.json.impl.reader;

import java.io.IOException;
import java.math.BigDecimal;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonReadContext;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author japod
 */
public class JacksonRootAddingParser extends JsonParser {

    enum State { START, AFTER_SO, AFTER_FN, INNER, END };

    String rootName;
    JsonParser parser;
    JsonToken currentToken;
    State state;

    public static JsonParser createRootAddingParser(JsonParser parser, String rootName) {
        return new JacksonRootAddingParser(parser, rootName);
    }

    private JacksonRootAddingParser(){}

    private JacksonRootAddingParser(JsonParser parser, String rootName) {
        this.parser = parser;
        this.state = State.START;
        this.rootName = rootName;
    }

    @Override
    public void enableFeature(Feature feature) {
        parser.enableFeature(feature);
    }

    @Override
    public void disableFeature(Feature feature) {
        parser.disableFeature(feature);
    }

    @Override
    public void setFeature(Feature feature, boolean isSet) {
        parser.setFeature(feature, isSet);
    }

    @Override
    public boolean isFeatureEnabled(Feature feature) {
        return parser.isFeatureEnabled(feature);
    }

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
        switch (state) {
            case START :
                state = State.AFTER_SO;
                currentToken = JsonToken.START_OBJECT;
                return currentToken;
            case AFTER_SO :
                state = State.AFTER_FN;
                currentToken = JsonToken.FIELD_NAME;
                return currentToken;
            case AFTER_FN :
                state = State.INNER;
            case INNER :
                currentToken = parser.nextToken();
                if (currentToken == null) {
                    state = State.END;
                    currentToken = JsonToken.END_OBJECT;
                }
                return currentToken;
            case END :
            default :
                    currentToken = null;
                    return currentToken;
        }
    }

    @Override
    public void skipChildren() throws IOException, JsonParseException {
        parser.skipChildren();
    }

    @Override
    public JsonToken getCurrentToken() {
        switch (state) {
            case START :
                return null;
            case AFTER_SO :
                return currentToken;
            case AFTER_FN :
                return currentToken;
            default:
                return currentToken;
        }
    }

    @Override
    public boolean hasCurrentToken() {
        switch (state) {
            case START :
                return false;
            case AFTER_SO :
                return true;
            case AFTER_FN :
                return true;
            default:
                return currentToken != null;
        }
    }

    @Override
    public String getCurrentName() throws IOException, JsonParseException {
        switch (state) {
            case START :
                return null;
            case AFTER_SO :
                return null;
            case AFTER_FN :
                return rootName;
            case INNER :
                return parser.getCurrentName();
            default:
                return null;
        }
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }

    @Override
    public JsonReadContext getParsingContext() {
        return parser.getParsingContext();
    }

    @Override
    public JsonLocation getTokenLocation() {
        return parser.getTokenLocation();
    }

    @Override
    public JsonLocation getCurrentLocation() {
        return parser.getCurrentLocation();
    }

    @Override
    public String getText() throws IOException, JsonParseException {
        return parser.getText();
    }

    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        return parser.getTextCharacters();
    }

    @Override
    public int getTextLength() throws IOException, JsonParseException {
        return parser.getTextLength();
    }

    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        return parser.getTextOffset();
    }

    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        return parser.getNumberValue();
    }

    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
        return parser.getNumberType();
    }

    @Override
    public int getIntValue() throws IOException, JsonParseException {
        return parser.getIntValue();
    }

    @Override
    public long getLongValue() throws IOException, JsonParseException {
        return parser.getLongValue();
    }

    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        return parser.getDoubleValue();
    }

    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        return parser.getDecimalValue();
    }
}

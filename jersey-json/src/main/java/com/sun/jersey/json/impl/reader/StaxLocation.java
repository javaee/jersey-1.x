package com.sun.jersey.json.impl.reader;

import javax.xml.stream.Location;
import org.codehaus.jackson.JsonLocation;

class StaxLocation implements Location {

    int charOffset = -1;
    int column = -1;
    int line = -1;

    StaxLocation(final int charOffset, final int column, final int line) {
        super();
        this.charOffset = charOffset;
        this.column = column;
        this.line = line;
    }

    StaxLocation(final JsonLocation location) {
        this((int)location.getCharOffset(), location.getColumnNr(), location.getLineNr());
    }

    StaxLocation(final JsonLexer lexer) {
        this(lexer.getCharOffset(), lexer.getColumn(), lexer.getLineNumber());
    }

    public int getCharacterOffset() {
        return charOffset;
    }

    public int getColumnNumber() {
        return column;
    }

    public int getLineNumber() {
        return line;
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return null;
    }
}

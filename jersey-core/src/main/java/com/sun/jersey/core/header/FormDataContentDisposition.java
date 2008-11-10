/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.core.header;

import com.sun.jersey.core.header.reader.HttpHeaderReader;
import java.text.ParseException;

/**
 * A form-data content disposition header.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class FormDataContentDisposition extends ContentDisposition {
    private String name;

    public FormDataContentDisposition(String header) throws ParseException {
        this(HttpHeaderReader.newInstance(header));
    }

    public FormDataContentDisposition(HttpHeaderReader reader) throws ParseException {
        super(reader);
        if (!getType().equals("form-data")) {
            throw new IllegalArgumentException("The content dispostion type is not equal to form-data");
        }

        name = getParameters().get("name");
        if (name == null) {
            throw new IllegalArgumentException("The name parameter is not present");
        }
    }

    /**
     * Get the name parameter.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
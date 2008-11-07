/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.core.header;

import com.sun.jersey.core.header.reader.HttpHeaderReader;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * A content disposition header.
 * 
 * @author Paul.Sandoz@Sun.Com
 */
public class ContentDisposition {
    private String type;

    private Map<String, String> parameters;
    
    private String fileName;

    private Date creationDate;

    private Date modificationDate;

    private Date readDate;

    private int size;

    public ContentDisposition(String header) throws ParseException {
        this(HttpHeaderReader.newInstance(header));
    }

    public ContentDisposition(HttpHeaderReader reader) throws ParseException {
        reader.hasNext();

        type = reader.nextToken();

        if (reader.hasNext())
            parameters = HttpHeaderReader.readParameters(reader);        
        if (parameters == null)
            parameters = Collections.emptyMap();

        createParameters();
    }

    /**
     * Get the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the parameters.
     *
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Get the filename parameter.
     * 
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get the creation-date parameter.
     *
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Get the modification-date parameter.
     *
     * @return the modificationDate
     */
    public Date getModificationDate() {
        return modificationDate;
    }

    /**
     * Get the read-date parameter.
     *
     * @return the readDate
     */
    public Date getReadDate() {
        return readDate;
    }

    /**
     * Get the size parameter.
     * 
     * @return the size
     */
    public int getSize() {
        return size;
    }

    private void createParameters() throws ParseException {
        fileName = parameters.get("filename");

        creationDate = createDate("creation-date");

        modificationDate = createDate("modification-date");

        readDate = createDate("read-date");

        size = createInt("size");
    }

    private Date createDate(String name) throws ParseException {
        String value = parameters.get(name);
        if (value == null)
            return null;
        return HttpDateFormat.getPreferedDateFormat().parse(value);
    }

    private int createInt(String name) throws ParseException {
        String value = parameters.get(name);
        if (value == null)
            return -1;
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParseException("Error parsing size parameter of value, " + value, 0);
        }
    }
}
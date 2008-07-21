/*
 * $Id: $ (c)
 * Copyright 2008 freiheit.com technologies GmbH
 *
 * Created on Jun 7, 2008
 *
 * This file contains unpublished, proprietary trade secret information of
 * freiheit.com technologies GmbH. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * freiheit.com technologies GmbH.
 */
package com.sun.jersey.taglets.test;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Jun 7, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class TestResource {
    
    
    /**
     * Here's my comment text.
     * @resource.example {@link #EXAMPLE}
     * 
     * @return a string
     * @author Martin Grotzke
     */
    public String getName() {
        return "foo";
    }
    
    private static final Item EXAMPLE = new Item();
    static {
        EXAMPLE.setValue( "foo" );
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    @XmlRootElement(name = "item")
    static class Item {
        
        

        @XmlValue
        protected String value;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

    }

}

package com.sun.jersey.samples.jaxb;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class JAXBXmlType {

    public String value;

    public JAXBXmlType() {
    }

    public JAXBXmlType(String str) {
        value = str;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof JAXBXmlType)) 
            return false;
        return ((JAXBXmlType) o).value.equals(value);
    }
}

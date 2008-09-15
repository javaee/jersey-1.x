package com.sun.jersey.samples.jaxb;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JAXBXmlRootElement {

    public String value;

    public JAXBXmlRootElement() {
    }

    public JAXBXmlRootElement(String str) {
        value = str;
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof JAXBXmlRootElement)) 
            return false;
        return ((JAXBXmlRootElement) o).value.equals(value);
    }
}

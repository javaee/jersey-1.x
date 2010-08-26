package com.sun.jersey.samples.multipart.resources;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Bean {

    public String value;

    public Bean() {
    }

    public Bean(String str) {
        value = str;
    }
}

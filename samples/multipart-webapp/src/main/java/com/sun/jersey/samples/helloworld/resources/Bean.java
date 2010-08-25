package com.sun.jersey.samples.helloworld.resources;

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

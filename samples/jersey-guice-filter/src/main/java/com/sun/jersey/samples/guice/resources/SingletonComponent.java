package com.sun.jersey.samples.guice.resources;

import com.google.inject.Singleton;

@Singleton
public class SingletonComponent {

    public String toString() {
        return "SINGLETON: " + Integer.toHexString(hashCode());
    }
}

package com.sun.jersey.qe.tests.guice.resources;

import com.google.inject.Singleton;

@Singleton
public class SingletonComponent {

    public String toString() {
        return "SINGLETON: " + Integer.toHexString(hashCode());
    }
}

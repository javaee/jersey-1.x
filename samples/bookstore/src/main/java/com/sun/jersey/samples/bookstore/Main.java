/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.jersey.samples.bookstore;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import org.glassfish.embed.GlassFish;
import org.glassfish.embed.ScatteredWar;

/**
 *
 * @author japod
 */
public class Main {

    public static final int HttpPort = 8080;

    public static GlassFish startApplication() throws Exception {
        GlassFish glassfishInstance = new GlassFish(8080);

        Collection<URL> classpath = new HashSet<URL>();
        classpath.add(new File("target/lib/jstl.jar").toURI().toURL());
        classpath.add(new File("target/lib/standard.jar").toURI().toURL());
        classpath.add(new File("target/lib/jersey-server.jar").toURI().toURL());
        classpath.add(new File("target/classes").toURI().toURL());
        classpath.add(new File("src/main/resources").toURI().toURL());

        ScatteredWar war = new ScatteredWar(
                "bookstore",
                new File("src/main/webapp"),
                null,
                classpath);
        glassfishInstance.deploy(war);
        return glassfishInstance;
    }

    public static void main(String[] args) throws Exception {
        GlassFish glassfishInstance = startApplication();
        System.out.println("Press something...");
        System.in.read();
        glassfishInstance.stop();
    }
}

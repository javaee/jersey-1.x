/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.jersey.impl.container.config;

import com.sun.jersey.core.spi.scanning.ScannerListener;
import com.sun.jersey.core.spi.scanning.uri.JarZipSchemeScanner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author paulsandoz
 */
public class JarZipSchemeScannerTest extends AbstractConfigTester {
    
    public JarZipSchemeScannerTest(String testName) {
        super(testName);
    }

    public void testJarUrl() throws Exception {
        String name = "a bc";
        String loc = "/com/sun/jersey/impl/container/config/toplevel";
        
        File f = createJarFile(name, Suffix.jar, "target/test-classes/",
        "com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class",
        "com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
        );

        JarZipSchemeScanner scanner = new JarZipSchemeScanner();
        URI u = UriBuilder.fromPath("").scheme("jar").
                schemeSpecificPart(f.toURI().toASCIIString() + "!" + loc).build();
        final Set<String> names = new HashSet<String>();
        scanner.scan(u, new ScannerListener() {

            public boolean onAccept(String name) {
                return true;
            }

            public void onProcess(String name, InputStream in) throws IOException {
                names.add(name);
            }
        });
        assertEquals(2, names.size());
        assertTrue(names.contains("com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class"));
        assertTrue(names.contains("com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class"));
    }

    public void testWebLogicZipUrl() throws Exception {
        String name = "a bc";
        String loc = "/com/sun/jersey/impl/container/config/toplevel";

        File f = createJarFile(name, Suffix.zip, "target/test-classes/",
        "com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class",
        "com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PublicClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PackageClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$ProtectedClass.class",
        "com/sun/jersey/impl/container/config/innerstatic/InnerStaticClass$PrivateClass.class"
        );

        JarZipSchemeScanner scanner = new JarZipSchemeScanner();
        URI u = UriBuilder.fromPath("").scheme("zip").
                schemeSpecificPart(f.toURI().getRawPath() + "!" + loc).build();
        final Set<String> names = new HashSet<String>();
        scanner.scan(u, new ScannerListener() {

            public boolean onAccept(String name) {
                return true;
            }

            public void onProcess(String name, InputStream in) throws IOException {
                names.add(name);
            }
        });
        assertEquals(2, names.size());
        assertTrue(names.contains("com/sun/jersey/impl/container/config/toplevel/PublicRootResourceClass.class"));
        assertTrue(names.contains("com/sun/jersey/impl/container/config/toplevel/PackageRootResourceClass.class"));
    }

}

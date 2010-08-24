/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.oauth.tests;

import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Peforms a full integration test between a test OAuth server and OAuth
 * client. The tests attempt as much as possible to follow the sample
 * appendices of the OAuth specification, though the hostname and port
 * differs (it's localhost in the tests, not example.com).
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 */
public class IntegrationTest extends TestCase {

    /**
     * Creates the test case.
     *
     * @param testName name of the test case.
     */
    public IntegrationTest(String testName) {
        super(testName);
    }

    /**
     * Returns the suite of tests being tested.
     */
    public static Test suite() {
        return new TestSuite(IntegrationTest.class);
    }

    /**
     * Performs the integration test.
     */
    public void testIntegration() {
        String host = "localhost";
        int port = getenv("JERSEY_HTTP_PORT", 9998);
        try { Server.start(host, port); }
        catch (IOException ioe) { fail(ioe.getMessage()); }
        Client.execute(host, port);
        Server.stop();
    }

    /**
     * Gets an environment variable as an integer, defaulting value if not
     * set or unparseable.
     */
    private static int getenv(String name, int _default) {
        if (name == null) { return _default; }
        String value = System.getenv(name);
        if (value == null) { return _default; }
        try { return Integer.parseInt(value); }
        catch (NumberFormatException nfe) { return _default; }
    }
}

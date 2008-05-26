/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */

/*
 * AbstractResource.java
 */
package com.sun.jersey.api.model;

import com.sun.jersey.api.model.AbstractResourceMethod;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class AbstractResourceMethodTest extends TestCase {

    public class TestResource {

        public void getMethod() {
        }

        public void putMethod() {
        }

        public void postMethod() {
        }

        public void deleteMethod() {
        }
    }

    /**
     * Test of getHttpMethod method, of class AbstractResourceMethod.
     */
    public void testGetHttpMethod() throws NoSuchMethodException {
        AbstractResourceMethod arm = new AbstractResourceMethod(
                null, TestResource.class.getMethod("getMethod"), "PUT");
        assertEquals("PUT", arm.getHttpMethod());
    }
}

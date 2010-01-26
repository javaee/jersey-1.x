/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package com.sun.jersey.json.impl;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.jersey.api.json.JSONUnmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public class InheritanceTest extends TestCase {

    final AnimalList one = TestHelper.createTestInstance(AnimalList.class);

    public void testBadgerfish() throws Exception {
        System.out.println("\nTesting BadgerFish: ------------------------");
        tryListWithConfiguration(JSONConfiguration.badgerFish().build());
        tryIndividualsWithConfiguration(JSONConfiguration.badgerFish().build());
    }

    public void testMappedJettison() throws Exception {
        System.out.println("\nTesting Mapped Jettison: ---------------------");
        Map<String, String> ns2json = new HashMap<String, String>();
        ns2json.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        tryListWithConfiguration(JSONConfiguration.mappedJettison().xml2JsonNs(ns2json).build());
        tryIndividualsWithConfiguration(JSONConfiguration.mappedJettison().xml2JsonNs(ns2json).build());
    }

    public void testNatural() throws Exception {
        System.out.println("\nTesting Natural: -------------------------");
        // TODO: a patch applied at jaxb trunk to add a new utility method on UnmarshallingContext
        //            after this gets tested and make it to a release of jaxb, we can uncomment appropriate
        //            stuff on Jersey side, and the following should work
        //tryListWithConfiguration(JSONConfiguration.natural().build());
        tryIndividualsWithConfiguration(JSONConfiguration.natural().rootUnwrapping(false).build());
    }

    public void testMapped() throws Exception {
        System.out.println("\nTesting Mapped: -------------------------");
        // TODO: fix this
        Map<String, String> ns2json = new HashMap<String, String>();
        ns2json.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        tryListWithConfiguration(JSONConfiguration.mapped().xml2JsonNs(ns2json).build());
        tryIndividualsWithConfiguration(JSONConfiguration.mapped().rootUnwrapping(false).build());
    }

    private void tryListWithConfiguration(JSONConfiguration configuration) throws Exception {

        final JSONJAXBContext ctx = new JSONJAXBContext(configuration, AnimalList.class, Animal.class, Dog.class, Cat.class);
        final JSONMarshaller jm = ctx.createJSONMarshaller();
        final JSONUnmarshaller ju = ctx.createJSONUnmarshaller();
        final StringWriter sw = new StringWriter();

        AnimalList two;
        jm.marshallToJSON(one, sw);

        System.out.println(String.format("Marshalled: %s", sw));

        two = ju.unmarshalFromJSON(new StringReader(sw.toString()), AnimalList.class);

        assertEquals(one, two);
        for (int i = 0; i < one.animals.size(); i++) {
            assertEquals(one.animals.get(i).getClass(), two.animals.get(i).getClass());
        }
    }

    private void tryIndividualsWithConfiguration(JSONConfiguration configuration) throws Exception {

        final JSONJAXBContext ctx = new JSONJAXBContext(configuration, AnimalList.class, Animal.class, Dog.class, Cat.class);
        final JSONMarshaller jm = ctx.createJSONMarshaller();
        final JSONUnmarshaller ju = ctx.createJSONUnmarshaller();


        Animal animalTwo;

        for (int i = 0; i < one.animals.size(); i++) {

            final StringWriter sw = new StringWriter();
            Animal animalOne = one.animals.get(i);

            jm.marshallToJSON(animalOne, sw);

            System.out.println(String.format("Marshalled: %s", sw));

            animalTwo = ju.unmarshalFromJSON(new StringReader(sw.toString()), Animal.class);

            assertEquals(animalOne, animalTwo);
            System.out.println(String.format("class one = %s; class two = %s", animalOne.getClass(), animalTwo.getClass()));
            assertEquals(animalOne.getClass(), animalTwo.getClass());
        }
    }
}

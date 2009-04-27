/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import junit.framework.TestCase;

/**
 *
 * @author Jakub.Podlesak@Sun.COM
 */
public class InheritanceTest extends TestCase {

    final boolean jsonEnabled = true;
    final AnimalList one = (AnimalList) AnimalList.createTestInstance();

    public void testBadgerfish() throws Exception {
        tryListWithConfiguration(JSONConfiguration.badgerFish().build());
        tryIndividualsWithConfiguration(JSONConfiguration.badgerFish().build());
    }

    public void testMappedJettison() throws Exception {
        Map<String, String> ns2json = new HashMap<String, String>();
        ns2json.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        tryListWithConfiguration(JSONConfiguration.mappedJettison().xml2JsonNs(ns2json).build());
        tryIndividualsWithConfiguration(JSONConfiguration.mappedJettison().xml2JsonNs(ns2json).build());
    }

    public void testNatural() throws Exception {
        // TODO: fix this
        //tryListWithConfiguration(JSONConfiguration.natural().build());
        tryIndividualsWithConfiguration(JSONConfiguration.natural().build());
    }

    public void testMapped() throws Exception {
        // TODO: fix this
        //tryListWithConfiguration(JSONConfiguration.mapped().build());
        tryIndividualsWithConfiguration(JSONConfiguration.mapped().build());
    }

    private void tryListWithConfiguration(JSONConfiguration configuration) throws Exception {

        final JSONJAXBContext ctx = new JSONJAXBContext(configuration, AnimalList.class, Animal.class, Dog.class, Cat.class);
        final JSONMarshaller jm = (JSONMarshaller) ctx.createMarshaller();
        final JSONUnmarshaller ju = (JSONUnmarshaller) ctx.createUnmarshaller();
        final StringWriter sw = new StringWriter();

        jm.setJsonEnabled(jsonEnabled);
        AnimalList two;
        jm.marshal(one, sw);

        System.out.println(String.format("%s", sw));

        ju.setJsonEnabled(jsonEnabled);

        if (configuration.isRootUnwrapping()) {
            final JAXBElement e = (JAXBElement) ju.unmarshal(new StringReader(sw.toString()), (Class) one.getClass());
            two = (AnimalList) e.getValue();
        } else {
            two = (AnimalList) ju.unmarshal(new StringReader(sw.toString()));
        }

        assertEquals(one, two);
        for (int i = 0; i < one.animals.size(); i++) {
            assertEquals(one.animals.get(i).getClass(), two.animals.get(i).getClass());
        }
    }

    private void tryIndividualsWithConfiguration(JSONConfiguration configuration) throws Exception {

        final JSONJAXBContext ctx = new JSONJAXBContext(configuration, AnimalList.class, Animal.class, Dog.class, Cat.class);
        final JSONMarshaller jm = (JSONMarshaller) ctx.createMarshaller();
        final JSONUnmarshaller ju = (JSONUnmarshaller) ctx.createUnmarshaller();


        jm.setJsonEnabled(jsonEnabled);
        ju.setJsonEnabled(jsonEnabled);

        Animal animalTwo;

        for (int i = 0; i < one.animals.size(); i++) {

            final StringWriter sw = new StringWriter();
            Animal animalOne = one.animals.get(i);

            jm.marshal(animalOne, sw);

            System.out.println(String.format("%s", sw));


            if (configuration.isRootUnwrapping()) {
                final JAXBElement e = (JAXBElement) ju.unmarshal(new StringReader(sw.toString()), (Class) animalOne.getClass());
                animalTwo = (Animal) e.getValue();
            } else {
                animalTwo = (Animal) ju.unmarshal(new StringReader(sw.toString()));
            }

            assertEquals(animalOne, animalTwo);
            assertEquals(animalOne.getClass(), animalTwo.getClass());
        }
    }
}

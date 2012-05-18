/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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
package com.sun.jersey.json.impl;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.json.impl.reader.JsonXmlStreamReader;

import junit.framework.TestCase;

/**
 * Test for JERSEY-954.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Michal Gajdos (michal.gajdos at oracle.com)
 */
public class JsonXmlStreamReaderInvalidInputTest extends TestCase {

    private final static List<String> invalidInputs = Arrays.asList(
            "{",
            "[",
            // only the above input made the reader stuck
            ",",
            "\"",
            "\'",
            "}",
            "{{",
            "}}",
            "}{",
            "]",
            "lojza",
            "\"12"
    );

    private final static List<String> terminatingInputs = Arrays.asList(
            "",
            "\"\"",
            "{}",
            "12"
    );

    private final JAXBContext ctx;
    private final List<String> failedInputs = new LinkedList<String>();

    public JsonXmlStreamReaderInvalidInputTest() throws Exception {
        this.ctx = JAXBContext.newInstance(TwoListsWrapperBean.class);
    }

    public void testTerminatingInputMappedNotation() throws Exception {
        testInvalidInput(terminatingInputs, JSONConfiguration.mapped().build(), false);
    }

    public void testInvalidInputMappedNotation() throws Exception {
        testInvalidInput(invalidInputs, JSONConfiguration.mapped().build(), true);
    }

    public void testTerminatingInputNaturalNotation() throws Exception {
        testInvalidInput(terminatingInputs, JSONConfiguration.natural().build(), false);
    }

    public void testInvalidInputNaturalNotation() throws Exception {
        testInvalidInput(invalidInputs, JSONConfiguration.natural().build(), true);
    }

    private void testInvalidInput(final Collection<String> terminatingInputs,
                                  final JSONConfiguration configuration, final boolean failIfUnmarshals) throws Exception {
        for (String input : terminatingInputs) {
            terminatesBeforeTimeout(ctx, configuration, input, failIfUnmarshals);
        }

        if (!failedInputs.isEmpty()) {
            if (failIfUnmarshals) {
                fail("Inputs \"" + failedInputs + "\" should not have been parsed.");
            } else {
                fail("Inputs \"" + failedInputs + "\" caused an infinite loop.");
            }
        }
    }

    public void terminatesBeforeTimeout(final JAXBContext jaxbContext,
                                        final JSONConfiguration config,
                                        final String input,
                                        final boolean failIfUnmarshals) throws Exception {

        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        final Callable<JAXBElement<TwoListsWrapperBean>> callable = new Callable<JAXBElement<TwoListsWrapperBean>>() {

            @Override
            public JAXBElement<TwoListsWrapperBean> call() throws Exception {
                JAXBElement<TwoListsWrapperBean> unmarshal = null;

                try {
                    unmarshal = unmarshaller.unmarshal(
                            JsonXmlStreamReader.create(
                                    new StringReader(input), config, null, TwoListsWrapperBean.class, jaxbContext, false),
                            TwoListsWrapperBean.class);
                } catch (Exception e) {
                    System.out.println(e);
                }

                return unmarshal;
            }

        };

        final Future<JAXBElement<TwoListsWrapperBean>> element = executor.submit(callable);
        executor.shutdown();
        final boolean terminatedInTime = executor.awaitTermination(5000, TimeUnit.MILLISECONDS);

        if ((element.get() != null && failIfUnmarshals) || !terminatedInTime) {
            failedInputs.add(input);
        }
    }

}
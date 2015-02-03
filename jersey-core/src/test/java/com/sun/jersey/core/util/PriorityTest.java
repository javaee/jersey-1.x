/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test priority annotation usage on types and instances.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class PriorityTest {

    public static final int D_MINUS_200 = PriorityUtil.DEFAULT_PRIORITY - 200;
    public static final int D_PLUS_300 = PriorityUtil.DEFAULT_PRIORITY + 300;
    public static final int D_PLUS_400 = PriorityUtil.DEFAULT_PRIORITY + 400;

    public PriorityTest() {
    }

    public static interface Base {
        int getPriority();
    }

    @Priority(D_MINUS_200)
    public static class A implements Base {

        @Override
        public int getPriority() {
            return D_MINUS_200;
        }
    }

    public static class B implements Base {

        @Override
        public int getPriority() {
            return PriorityUtil.DEFAULT_PRIORITY;
        }
    }

    @Priority(D_PLUS_300)
    public static class C implements Base {

        @Override
        public int getPriority() {
            return D_PLUS_300;
        }
    }

    @Priority(D_PLUS_400)
    public static class D implements Base {

        @Override
        public int getPriority() {
            return D_PLUS_400;
        }
    }

    /**
     * Test the priority comparator.
     */
    @Test
    public void testOrdering() {

        Base a = new A();
        Base b = new B();
        Base c = new C();
        Base d = new D();

        List<Base> l1 = new ArrayList<Base>();

        l1.add(d);
        l1.add(c);
        l1.add(b);
        l1.add(a);

        for (int i=0; i<20; i++) {
            Collections.shuffle(l1);
            Collections.sort(l1, PriorityUtil.INSTANCE_COMPARATOR);
            int p = Integer.MAX_VALUE;
            for (Base e : l1) {
                if (e.getPriority() > p) {
                    Assert.fail("Wrong order of list " + l1);
                }
                p = e.getPriority();
            }
        }
    }
}

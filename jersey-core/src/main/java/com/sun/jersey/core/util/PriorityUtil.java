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

import java.util.Comparator;

/**
 * Utility code for work with {@link Priority} annotated types.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class PriorityUtil {

    /**
     * Default priority value for factory types that do not have
     * their priority set explicitly using {@link Priority} annotation.
     */
    public static final int DEFAULT_PRIORITY = 100;

    /**
     * Convenience comparator instance.
     */
    public static final InstanceComparator INSTANCE_COMPARATOR = new InstanceComparator();

    /**
     * Comparator for instances of types annotated with Priority
     * annotation.
     */
    public static final class InstanceComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            return priorityOf(o2) - priorityOf(o1);
        }

        private int priorityOf(Object o) {
            final Priority priorityAnnotation = o.getClass().getAnnotation(Priority.class);
            return priorityAnnotation == null ? DEFAULT_PRIORITY : priorityAnnotation.value();
        }
    }

    /**
     * Comparator for types annotated with Priority
     * annotation.
     */
    public static class TypeComparator implements Comparator<Class<?>> {

        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            return priorityOf(o2) - priorityOf(o1);
        }

        private int priorityOf(Class<?> o) {
            final Priority priorityAnnotation = o.getAnnotation(Priority.class);
            return priorityAnnotation == null ? DEFAULT_PRIORITY : priorityAnnotation.value();
        }
    }
}
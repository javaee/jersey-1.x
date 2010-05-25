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

package com.sun.jersey.spi.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Tests:
 *   com.sun.jersey.impl.inject.InjectAnnotationInjectableTest
 *
 * TODO errors should have a push and pop context and when the last context
 * is popped off the stack if there are errors then all errors for all contexts
 * are logged and an exception is thrown.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class Errors {

    private static final Logger LOGGER = Logger.getLogger(Errors.class.getName());

    public static void innerClass(Class c) {
        LOGGER.warning("The inner class " + c.getName() + " is not a static inner class and cannot be instantiated.");
    }
    
    public static void nonPublicClass(Class c) {
        LOGGER.warning("The class " + c.getName() + " is a not a public class and cannot be instantiated.");
    }

    public static void missingDependency(Constructor ctor, int i) {
//        Class[] parameterTypes = ctor.getParameterTypes();
//        Type[] genericParameterTypes = ctor.getGenericParameterTypes();
//        // Workaround bug http://bugs.sun.com/view_bug.do?bug_id=5087240
//        if (parameterTypes.length != genericParameterTypes.length) {
//            Type[] _genericParameterTypes = new Type[parameterTypes.length];
//            _genericParameterTypes[0] = parameterTypes[0];
//            System.arraycopy(genericParameterTypes, 0, _genericParameterTypes, 1, genericParameterTypes.length);
//            genericParameterTypes = _genericParameterTypes;
//        }

        LOGGER.warning("Missing dependency for constructor " + ctor + " at parameter index " + i);
    }

    public static void missingDependency(Field f) {
        LOGGER.warning("Missing dependency for field: " + f.toGenericString());
    }

    public static void missingDependency(Method m, int i) {
        LOGGER.warning("Missing dependency for method " + m + " at parameter at index " + i);
    }

}

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
package com.sun.jersey.spi.inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * TODO do not use static thread local?
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class Errors {

    public static class ErrorMessagesException extends RuntimeException {
        public final List<ErrorMessage> messages;
        
        private ErrorMessagesException(List<ErrorMessage> messages) {
            this.messages = messages;
        }
    }

    public static class ErrorMessage {
        
        final String message;

        final boolean isFatal;

        private ErrorMessage(String message, boolean isFatal) {
            this.message = message;
            this.isFatal = isFatal;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + (this.message != null ? this.message.hashCode() : 0);
            hash = 37 * hash + (this.isFatal ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ErrorMessage other = (ErrorMessage) obj;
            if ((this.message == null) ? (other.message != null) : !this.message.equals(other.message)) {
                return false;
            }
            if (this.isFatal != other.isFatal) {
                return false;
            }
            return true;
        }
        public int hashcode() {return 0;}

    }

    private final ArrayList<ErrorMessage> messages = new ArrayList<ErrorMessage>(0);

    private int mark = -1;

    private int stack = 0;

    private boolean fieldReporting = true;

    private void _mark() {
        mark = messages.size();
    }

    private void _unmark() {
        mark = -1;
    }

    private void _reset() {
        if (mark >= 0 && mark < messages.size()) {
            messages.subList(mark, messages.size()).clear();
            _unmark();
        }
    }

    private void preProcess() {
        stack++;
    }

    private void postProcess(boolean throwException) {
        stack--;
        fieldReporting = true;

        if (stack == 0) {
            try {
                if (!messages.isEmpty()) {
                    processErrorMessages(throwException, messages);
                }
            } finally {
                errors.remove();
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Errors.class.getName());

    private static void processErrorMessages(boolean throwException, List<ErrorMessage> messages) {
        final StringBuilder sb = new StringBuilder();
        boolean isFatal = false;
        for (ErrorMessage em : messages) {
            if (sb.length() > 0) {
                sb.append("\n");
            }

            sb.append("  ");

            if (em.isFatal) {
                sb.append("SEVERE: ");
            } else {
                sb.append("WARNING: ");
            }
            isFatal |= em.isFatal;

            sb.append(em.message);
        }

        final String message = sb.toString();
        if (isFatal) {
            LOGGER.severe("The following errors and warnings have been detected with resource and/or provider classes:\n" + message);
            if (throwException) {
                throw new ErrorMessagesException(new ArrayList<ErrorMessage>(messages));
            }
        } else {
            LOGGER.warning("The following warnings have been detected with resource and/or provider classes:\n" + message);
        }
    }

    private static ThreadLocal<Errors> errors = new ThreadLocal<Errors>();

    public static interface Closure<T> {
        public T f();
    }

    public static <T> T processWithErrors(Closure<T> c) {
        Errors e = errors.get();
        if (e == null) {
            e = new Errors();
            errors.set(e);
        }
        e.preProcess();

        RuntimeException caught = null;
        try {
            return c.f();
        } catch (RuntimeException re) {
            // If a runtime exception is caught then report errors and
            // rethrow
            caught = re;
        } finally {
            e.postProcess(caught == null);
        }

        throw caught;
    }

    private static Errors getInstance() {
        Errors e = errors.get();
        // No error processing in scope
        if (e == null) {
            throw new IllegalStateException("There is no error processing in scope");
        }
        // The following should not be necessary but given the fragile nature of
        // static thread local probably best to add it in case some internals of
        // this class change
        if (e.stack == 0) {
            errors.remove();
            throw new IllegalStateException("There is no error processing in scope");
        }
        return e;
    }

    public static void mark() {
        getInstance()._mark();
    }

    public static  void unmark() {
        getInstance()._unmark();
    }

    public static void reset() {
        getInstance()._reset();
    }

    public static void error(String message) {
        error(message, true);
    }

    public static void error(String message, boolean isFatal) {
        final ErrorMessage em = new ErrorMessage(message, isFatal);
        getInstance().messages.add(em);
    }

    public int numberOfErrors() {
        return getInstance().messages.size();
    }
    
    public static void innerClass(Class c) {
        error("The inner class " + c.getName() + " is not a static inner class and cannot be instantiated.");
    }

    public static void nonPublicClass(Class c) {
        error("The class " + c.getName() + " is a not a public class and cannot be instantiated.");
    }

    public static void nonPublicConstructor(Class c) {
        error("The class " + c.getName() + " does not have a public constructor and cannot be instantiated.");
    }

    public static void abstractClass(Class c) {
        error("The class " + c.getName() + " is an abstract class and cannot be instantiated.");
    }

    public static void interfaceClass(Class c) {
        error("The class " + c.getName() + " is an interface and cannot be instantiated.");
    }

    public static void missingDependency(Constructor ctor, int i) {
        error("Missing dependency for constructor " + ctor + " at parameter index " + i);
    }

    public static void setReportMissingDependentFieldOrMethod(boolean fieldReporting) {
        getInstance().fieldReporting = fieldReporting;
    }

    public static boolean getReportMissingDependentFieldOrMethod() {
        return getInstance().fieldReporting;
    }

    public static void missingDependency(Field f) {
        if (getReportMissingDependentFieldOrMethod()) {
            error("Missing dependency for field: " + f.toGenericString());
        }
    }
    
    public static void missingDependency(Method m, int i) {
        if (getReportMissingDependentFieldOrMethod()) {
            error("Missing dependency for method " + m + " at parameter at index " + i);
        }
    }
}

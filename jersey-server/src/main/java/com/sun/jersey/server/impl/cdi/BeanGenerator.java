/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.server.impl.cdi;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Generates any number of plain bean classes on the fly, loading them with
 * the thread context class loader.
 *
 * Generated bean classes have a single, public, no-arg constructor.
 *
 * @author robc
 */
public class BeanGenerator {

    private static final Logger LOGGER = Logger.getLogger(CDIExtension.class.getName());
    private String prefix;
    private Method defineClassMethod;
    private int generatedClassCounter = 0;

    BeanGenerator(String prefix) {
        this.prefix = prefix;
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            defineClassMethod =
                AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){
                    public Method run() throws Exception{
                        Class classLoaderClass = Class.forName("java.lang.ClassLoader");
                        Method method = classLoaderClass.getDeclaredMethod(
                            "defineClass",
                            new Class[] { String.class, byte[].class, int.class, int.class });
                        method.setAccessible(true);
                        return method;
                    }
                });
        }
        catch (PrivilegedActionException e) {
            LOGGER.log(Level.SEVERE, "failed to access method ClassLoader.defineClass", e);
            // TODO - wrapping and rethrowing for now
            throw new RuntimeException(e);
        }
    }
    
    Class<?> createBeanClass() {
        ClassWriter writer = new ClassWriter(0);
        String name = prefix + Integer.toString(generatedClassCounter++);
        writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, name, null, "java/lang/Object", null);
        MethodVisitor methodVisitor = writer.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
        methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        methodVisitor.visitInsn(Opcodes.RETURN);
        methodVisitor.visitMaxs(1,1);
        methodVisitor.visitEnd();
        writer.visitEnd();
        byte[] bytecode = writer.toByteArray();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> result = (Class<?>) defineClassMethod.invoke(classLoader, name.replace("/","."), bytecode, 0, bytecode.length);
            LOGGER.fine("Created class " + result.getName());
            return result;
        }
        catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "error calling ClassLoader.defineClass", t);
            return null;
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;


/**
 * A proxy invocation handler that delegates all methods to a thread
 * local instance
 */
public class ThreadLocalInvoker<T> implements InvocationHandler {
    private ThreadLocal<T> threadLocalInstance = new ThreadLocal<T>();
    
    public void set(T threadLocalInstance) {
        this.threadLocalInstance.set(threadLocalInstance);
    }
    
    public T get() {
        return this.threadLocalInstance.get();
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (threadLocalInstance.get() == null)
            return null;
        return method.invoke(threadLocalInstance.get(), args);
    }
}
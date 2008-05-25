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

package com.sun.jersey.impl.container.servlet;

import com.sun.jersey.impl.ThreadLocalInvoker;
import java.lang.reflect.Method;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * A proxy invocation handler that delegates all methods to a thread
 * local instance
 */
public class ThreadLocalNamedInvoker<T> extends ThreadLocalInvoker<T> {
    
    private String name;
    
    /**
     * @param name the JNDI name at which an instance of T can be found
     */
    public ThreadLocalNamedInvoker(String name) {
        this.name = name;
    }
    
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // if no instance yet exists for the current thread then look one up
        // and stash it
        if (this.get() == null) {
            Context ctx = new InitialContext();
            T t = (T) ctx.lookup(name);
            this.set(t);
        }
        return super.invoke(proxy,method,args);
    }    
}

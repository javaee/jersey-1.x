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
 * Contributor(s): Gili Tzabari
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
package com.sun.jersey.client.impl.async;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.async.FutureListener;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public abstract class FutureClientResponseListener<T> extends FutureTask<T> 
        implements FutureListener<ClientResponse> {
    
    private static final Callable NO_OP_CALLABLE = new Callable() {
        public Object call() throws Exception {
            throw new IllegalStateException();
        }
    };

    private Future<ClientResponse> f;

    public FutureClientResponseListener() {
        super(NO_OP_CALLABLE);
    }

    public void setCancelableFuture(Future<ClientResponse> f) {
        this.f = f;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (f.isCancelled()) {
            if (!super.isCancelled()) {
                super.cancel(true);
            }
            return false;
        }
        
        boolean cancelled = f.cancel(mayInterruptIfRunning);
        if (cancelled) {
            super.cancel(true);
        }

        return cancelled;
    }

    @Override
    public boolean isCancelled() {
        if (f.isCancelled()) {
            if (!super.isCancelled()) {
                super.cancel(true);
            }
            return true;
        } else {
            return false;
        }
    }

    // FutureListener

    public void onComplete(Future<ClientResponse> response) {
        try {
            set(get(response.get()));
        } catch (CancellationException ex) {
            super.cancel(true);
        } catch (ExecutionException ex) {
            setException(ex.getCause());
        } catch (Throwable t) {
            setException(t);
        }
    }

    protected abstract T get(ClientResponse response);

}
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

package com.sun.jersey.samples.bookmark.util.tx;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.WebApplicationException;

/**
 *
 * @author Paul Sandoz
 */
public final class TransactionManager {
    
    public static void manage(Transactional t) {
        UserTransaction utx = getUtx();
        try {
            utx.begin();
            if (t.joinTransaction)
                t.em.joinTransaction();
            t.transact();
            utx.commit();
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (SystemException se) {
                throw new WebApplicationException(se);
            }
            throw new WebApplicationException(e);
        } finally {
            t.em.close();
        }
    }
    
    private static UserTransaction getUtx() {
        try {
            InitialContext ic = new InitialContext();
            return (UserTransaction)ic.
                    lookup("java:comp/UserTransaction");
        } catch (NamingException ne) {
            throw new WebApplicationException(ne);
        }
    }
}

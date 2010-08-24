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

package com.sun.jersey.oauth.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Tracks the nonces for a given consumer key and/or token. Automagically
 * ensures timestamp is monotonically increasing and tracks all nonces
 * for a given timestamp.
 *
 * @author Paul C. Bryan <pbryan@sun.com>
 * @author Martin Matula
 */
public class NonceManager
{
    /** The maximum valid age of a nonce timestamp, in milliseconds. */
    private final long maxAge;

    /** Verifications to perform on average before performing garbage collection. */
    private final int gcPeriod;

    /** Counts number of verification requests performed to schedule garbage collection. */
    private int gcCounter = 0;

    /** Maps keys to nonces. */
    private final Map<String, Map<Long, Set<String>>> map = new HashMap<String, Map<Long, Set<String>>>();

    /** Index by timestamps (for garbage collection) */
    private final SortedMap<Long, String> tsIndex = new TreeMap<Long, String>();

    /**
     * TODO: Description.
     *
     * @param maxAge the maximum valid age of a nonce timestamp, in milliseconds.
     * @param gcPeriod verifications to perform on average before performing garbage collection.
     */
    public NonceManager(long maxAge, int gcPeriod)
    {
        if (maxAge <= 0 || gcPeriod <= 0) {
            throw new IllegalArgumentException();
        }

        this.maxAge = maxAge;
        this.gcPeriod = gcPeriod;
    }

    /**
     * Evaluates the timestamp/nonce combination for validity, storing and/or
     * clearing nonces as required.
     *
     * @param timestamp the oauth_timestamp value for a given consumer request.
     * @param nonce the oauth_nonce value for a given consumer request.
     * @return true if the timestamp/nonce are valid.
     */
    public synchronized boolean verify(String key, String timestamp, String nonce)
    {
        long now = System.currentTimeMillis();

        // convert timestap to milliseconds since epoch to deal with uniformly
        long stamp = longValue(timestamp) * 1000;

        // invalid timestamp supplied; automatically invalid
        if (stamp + maxAge < now) {
            return false;
        }

        Map<Long, Set<String>> timestamps = map.get(key);
        if (timestamps == null) {
            timestamps = new HashMap<Long, Set<String>>();
            map.put(key, timestamps);
        }

        Set<String> nonces = timestamps.get(stamp);
        if (nonces == null) {
            nonces = new HashSet<String>();
            timestamps.put(stamp, nonces);
            tsIndex.put(stamp, key);
        }

        boolean result = nonces.add(nonce);

        // perform garbage collection if counter is up to established number of passes
        if (++gcCounter >= gcPeriod) {
            gcCounter = 0;
            SortedMap<Long, String> toCollect = tsIndex.headMap(now - maxAge);
            for (Map.Entry<Long, String> entry : toCollect.entrySet()) {
                map.get(entry.getValue()).remove(entry.getKey());
            }
            toCollect.clear();
        }

        // returns false if nonce already encountered for given timestamp
        return result;
    }

    private static long longValue(String value) {
        try {
            return Long.valueOf(value);
        }
        catch (NumberFormatException nfe) {
            return -1;
        }
    }
}


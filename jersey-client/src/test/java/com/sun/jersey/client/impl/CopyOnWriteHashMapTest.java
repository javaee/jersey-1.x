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

package com.sun.jersey.client.impl;

import com.sun.jersey.client.impl.CopyOnWriteHashMap;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author pavel.bucek@oracle.com
 */
public class CopyOnWriteHashMapTest extends TestCase {
    private CopyOnWriteHashMap<Integer, Object> instance;

    {
        instance = new CopyOnWriteHashMap<Integer, Object>();
        instance.put(-1, "something");
    }

    private Map<Integer, Object> getView() throws Exception {
      return instance.view;
    }

    private void assertViewsEqual(Map<Integer, Object> oldView, Map<Integer, Object> newView) {
        assertEquals(oldView, newView);
    }

    private void assertViewsNotEqual(Map<Integer, Object> oldView, Map<Integer, Object> newView) {
        assertTrue(!oldView.equals(newView));
    }

    public void testClone() {
        CopyOnWriteHashMap<String, String> map1 = new CopyOnWriteHashMap<String, String>();
        map1.put("a", "val");
        CopyOnWriteHashMap<String, String> map2 = map1.clone();
        map2.put("b", "val");

        assertTrue(map1.containsKey("a"));
        assertFalse(map1.containsKey("b"));
        assertTrue(map2.containsKey("a"));
        assertTrue(map2.containsKey("b"));
    }

    public void testSizeKeepsView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.size();
        Map<Integer, Object> newView = getView();

        assertViewsEqual(oldView, newView);
    }

    public void testIsEmptyKeepsView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.isEmpty();
        Map<Integer, Object> newView = getView();

        assertViewsEqual(oldView, newView);
    }

    public void testContainsKeyKeepsView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.containsKey("hunh");
        Map<Integer, Object> newView = getView();

        assertViewsEqual(oldView, newView);
    }

    public void testContainsValueKeepsView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.containsValue("whoa, Neo");
        Map<Integer, Object> newView = getView();

        assertViewsEqual(oldView, newView);
    }

    public void testGetKeepsView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.get("peanut butter sandwich");
        Map<Integer, Object> newView = getView();

        assertViewsEqual(oldView, newView);
    }

    public void testKeySetKeepsView() throws Exception {
        Map<Integer, Object> oldView = getView();
        Set<Integer> actual = instance.keySet();
        Map<Integer, Object> newView = getView();

        Set<Integer> expected = new HashSet<Integer>();
        expected.add(-1);
        assertEquals(expected, actual);
        assertViewsEqual(oldView, newView);
    }

    public void testValuesKeepsView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.values();
        Map<Integer, Object> newView = getView();

        assertEquals(1, instance.size());
        assertViewsEqual(oldView, newView);
    }

    public void testToStringKeepsView()  throws Exception{
        Map<Integer, Object> oldView = getView();
        instance.toString();
        Map<Integer, Object> newView = getView();

        assertViewsEqual(oldView, newView);
    }

    public void testPutChangesView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.put(-2, "heyya");
        Map<Integer, Object> newView = getView();

        assertEquals(2, instance.size());
        assertViewsNotEqual(oldView, newView);
    }

    public void testRemoveChangesView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.remove(-1);
        Map<Integer, Object> newView = getView();

        assertTrue(instance.isEmpty());
        assertViewsNotEqual(oldView, newView);
    }

    public void testPutAllChangesView() throws Exception{
        Map<Integer, Object> oldView = getView();
        final HashMap<Integer, Object> newValues = new HashMap<Integer, Object>();
        newValues.put(1, 10);
        newValues.put(2, 20);
        instance.putAll(newValues);
        Map<Integer, Object> newView = getView();

        assertEquals(3, instance.size());
        assertViewsNotEqual(oldView, newView);
    }

    public void testClearChangesView() throws Exception {
        Map<Integer, Object> oldView = getView();
        instance.clear();
        Map<Integer, Object> newView = getView();

        assertTrue(instance.isEmpty());
        assertViewsNotEqual(oldView, newView);
    }

    private boolean updaterFailure;

    private class Updater implements Runnable {
        private final int first;
        private final int last;
        private final boolean doClears;
        private final Thread thread;
        private volatile int count = -1;

        Updater(int first, int last, boolean doClears) {
            this.first = first;
            this.last = last;
            this.doClears = doClears;
            thread = new Thread(this);
        }

        @Override
        public void run() {
            updaterFailure = false;
            int i = -1;
            try {
                System.out.println("Updater starting at " + first + " has started");
                for (i = first; i <= last; i ++) {
                    count = i; // volatile write
                    instance.put(i, i);
                    if (doClears) {
                        instance.clear();
                    }
                    instance.entrySet();
                }
                System.out.println("Updater starting at " + first + " is done");
            } catch (Exception e) {
                updaterFailure = true;
                System.out.println("Error running puts, i = " + i);
                e.printStackTrace();
            }
        }
    }

    /**
     * Just for giggles, try running this test with {@link com.sun.jersey.client.impl.CopyOnWriteHashMap}.  It
     * <em>never</em> passes. Usually one or more threads fail to stop:
     *
     * <pre>
     *     08:44:59.622 [main] INFO  c.n.trace.NikeCopyOnWriteHashMapTest - testConcurrentUpdate still waiting on 1 thread(s)
     *     08:45:04.623 [main] WARN  c.n.trace.NikeCopyOnWriteHashMapTest - Stopped updater first=34000 count=34951
     * </pre>
     *
     * However I have also seen:
     *
     * <pre>
     * java.lang.ArrayIndexOutOfBoundsException: 12214
     *         at java.util.HashMap.createEntry(HashMap.java:896) ~[na:1.7.0_67]
     *         at java.util.HashMap.addEntry(HashMap.java:884) ~[na:1.7.0_67]
     *         at java.util.HashMap.put(HashMap.java:505) ~[na:1.7.0_67]
     *         at com.sun.jersey.client.impl.CopyOnWriteHashMap.put(CopyOnWriteHashMap.java:121) ~[jersey-client-1.18.1.jar:1.18.1]
     *         at com.nike.trace.NikeCopyOnWriteHashMapTest$Updater.run(NikeCopyOnWriteHashMapTest.java:213) ~[test/:na]
     *         at java.lang.Thread.run(Thread.java:745) [na:1.7.0_67]
     * </pre>
     *
     * as well as, occasionally, a very rapid execution followed by:
     * <pre>
     *    java.lang.AssertionError:
     *    Expected :40001
     *    Actual   :32573
     * </pre>
     */
    public void testConcurrentUpdate() throws Exception {
        final int numThreads = 20;
        final int updatesPerThread = 2000;
        final int maxWait = 60;
        final int pause = 5;
        List<Updater> updaters = new ArrayList<Updater>(numThreads);

        for (int i = 0; i < numThreads; i ++) {
            Updater updater = new Updater(i * updatesPerThread, (i + 1) * updatesPerThread - 1, false);
            updaters.add(updater);
            updater.thread.start();
        }

        long tilt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxWait);
        for (;;) {
            final long now = System.currentTimeMillis();
            if (now > tilt) {
                for (Updater updater: updaters) {
                    Thread t = updater.thread;
                    if (t.isAlive()) {
                        t.interrupt();
                        System.out.println("Stopped updater first=" + updater.first + " count=" + updater.count);
                    }
                }
                throw new AssertionError("Never finished");
            }

            Iterator<Updater> it = updaters.iterator();
            while (it.hasNext()) {
                Updater updater = it.next();
                if (!updater.thread.isAlive()) {
                    it.remove();
                }
            }
            if (updaters.isEmpty()) {
                break;
            }

            System.out.println("testConcurrentUpdate still waiting on " + updaters.size() + " thread(s)");
            Thread.sleep(TimeUnit.SECONDS.toMillis(pause));
        }

        // Expect every update to have inserted an entry into the map.
        assertEquals(1 + numThreads * updatesPerThread, instance.size());
        assertTrue(!updaterFailure);
    }

    /**
     * Attempts to reproduce the stack trace we see in Jersey, esp. CopyOnWriteHashMap.java:155. Failure modes seen are:
     *
     * <pre>
     * 08:03:15.196 [Thread-7] ERROR c.n.trace.NikeCopyOnWriteHashMapTest - Error running puts, i = 14011
     * java.lang.NullPointerException: null
     *         at com.sun.jersey.client.impl.CopyOnWriteHashMap.entrySet(CopyOnWriteHashMap.java:155) ~[jersey-client-1.18.1.jar:1.18.1]
     *         at com.nike.trace.NikeCopyOnWriteHashMapTest$Updater.run(NikeCopyOnWriteHashMapTest.java:224) ~[test/:na]
     *         at java.lang.Thread.run(Thread.java:745) [na:1.7.0_67]
     * </pre>
     * @throws Exception
     */
    public void testConcurrentClears() throws Exception {
        final int numThreads = 20;
        final int updatesPerThread = 2000;
        final int maxWait = 60;
        final int pause = 5;
        List<Updater> updaters = new ArrayList(numThreads);

        for (int i = 0; i < numThreads; i ++) {
            Updater updater = new Updater(i * updatesPerThread, (i + 1) * updatesPerThread - 1, true);
            updaters.add(updater);
            updater.thread.start();
        }

        long tilt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxWait);
        for (;;) {
            final long now = System.currentTimeMillis();
            if (now > tilt) {
                for (Updater updater: updaters) {
                    Thread t = updater.thread;
                    if (t.isAlive()) {
                        t.interrupt();
                        System.out.println("Stopped updater first=" + updater.first + " count=" + updater.count);
                    }
                }
                throw new AssertionError("Never finished");
            }

            Iterator<Updater> it = updaters.iterator();
            while (it.hasNext()) {
                Updater updater = it.next();
                if (!updater.thread.isAlive()) {
                    it.remove();
                }
            }
            if (updaters.isEmpty()) {
                break;
            }

            System.out.println("testConcurrentUpdate still waiting on " + updaters.size() + " thread(s)");
            Thread.sleep(TimeUnit.SECONDS.toMillis(pause));
        }

        // Expect every update to have finished by clearing the map
        assertEquals(0, instance.size());
        assertTrue(!updaterFailure);
    }

}

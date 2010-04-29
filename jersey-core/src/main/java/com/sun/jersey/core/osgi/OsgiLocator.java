/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.sun.jersey.core.osgi;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class OsgiLocator {

    private static Map<String, List<Callable<List<Class>>>> factories = new HashMap<String, List<Callable<List<Class>>>>();
    private static ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private OsgiLocator() {
    }

    public static void unregister(String id, Callable<List<Class>> factory) {
        lock.writeLock().lock();
        try {
            List<Callable<List<Class>>> l = factories.get(id);
            if (l != null) {
                l.remove(factory);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void register(String id, Callable<List<Class>> factory) {
        lock.writeLock().lock();
        try {
            List<Callable<List<Class>>> l = factories.get(id);
            if (l ==  null) {
                l = new ArrayList<Callable<List<Class>>>();
                factories.put(id, l);
            }
            l.add(factory);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static synchronized Class locate(String factoryId) {
        lock.readLock().lock();
        try {
            List<Callable<List<Class>>> l = factories.get(factoryId);
            if (l == null || l.isEmpty()) {
                return null;
            }
            
            Callable<List<Class>> c = l.get(l.size() - 1);
            List<Class> classes;
            try {
                classes = c.call();
            } catch (Exception e) {
                return null;
            }
            
            return classes.get(0);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static synchronized List<Class> locateAll(String factoryId) {
        List<Class> classes = new ArrayList<Class>();
        List<Callable<List<Class>>> l = factories.get(factoryId);
        if (l == null) {
            return classes;
        }
        
        for (Callable<List<Class>> c : l) {
            try {
                classes.addAll(c.call());
            } catch (Exception e) {
            }
        }
        return classes;
    }

}

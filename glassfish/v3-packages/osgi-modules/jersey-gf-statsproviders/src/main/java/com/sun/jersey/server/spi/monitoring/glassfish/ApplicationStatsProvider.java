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

package com.sun.jersey.server.spi.monitoring.glassfish;

import java.util.HashMap;
import java.util.Map;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

/**
 *
 * @author pavel.bucek@sun.com
 */
@ManagedObject
public class ApplicationStatsProvider {

    private Map<String, Long> rootResourceClassCounter;
    private Map<String, Long> resourceClassCounter;

    @ManagedAttribute(id="rootResourceClassHitCount-description")
    public String getRootResourceClassHitCountDesc() {
        return "Root resource class hit count";
    }

    private long rootResourceClassHitCountStartTime = new java.util.Date().getTime();
    private long rootResourceClassHitCountLastSampleTime;

    @ManagedAttribute(id="rootResourceClassHitCount-starttime")
    public long getRootResourceClassHitCountStartTime() {
        return rootResourceClassHitCountStartTime;
    }

    @ManagedAttribute(id="rootResourceClassHitCount-lastsampletime")
    public long getRootResourceClassHitCountLastSampleTime() {
        return rootResourceClassHitCountLastSampleTime;
    }

    @ManagedAttribute(id="rootResourceClassHitCount")
    public Map<String, Long> getRootResourceClassCounter() {
        return rootResourceClassCounter;
    }

    @ManagedAttribute(id="resourceClassHitCount-description")
    public String getResourceClassHitCountDesc() {
        return "Resource class hit count";
    }


    private long resourceClassHitCountStartTime = new java.util.Date().getTime();
    private long resourceClassHitCountLastSampleTime;

    @ManagedAttribute(id="resourceClassHitCount-starttime")
    public long getResourceClassHitCountStartTime() {
        return resourceClassHitCountStartTime;
    }

    @ManagedAttribute(id="resourceClassHitCount-lastsampletime")
    public long getResourceClassHitCountLastSampleTime() {
        return resourceClassHitCountLastSampleTime;
    }

    @ManagedAttribute(id="resourceClassHitCount")
    public Map<String, Long> getResourceClassCounter() {
        return resourceClassCounter;
    }


    public ApplicationStatsProvider() {
        rootResourceClassCounter = new HashMap<String, Long>();
        resourceClassCounter = new HashMap<String, Long>();
    }


    public void rootResourceClassHit(String resourceClassName) {

        // synchronized (rootResourceClassCounter)?

        if(rootResourceClassCounter.containsKey(resourceClassName)) {

            rootResourceClassCounter.put(
                    resourceClassName,
                    rootResourceClassCounter.get(resourceClassName) + 1
            );
        } else {
            rootResourceClassCounter.put(resourceClassName, new Long(1));
        }

        rootResourceClassHitCountLastSampleTime = new java.util.Date().getTime();
    }


    public void resourceClassHit(String resourceClassName) {

        // synchronized (resourceClassCounter)?

        if(resourceClassCounter.containsKey(resourceClassName)) {

            resourceClassCounter.put(
                    resourceClassName,
                    resourceClassCounter.get(resourceClassName) + 1
            );
        } else {
            resourceClassCounter.put(resourceClassName, new Long(1));
        }

        resourceClassHitCountLastSampleTime = new java.util.Date().getTime();
    }
}

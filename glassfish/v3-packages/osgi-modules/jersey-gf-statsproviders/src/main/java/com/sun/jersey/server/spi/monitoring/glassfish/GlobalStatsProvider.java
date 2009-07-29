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

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Domain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.external.probe.provider.annotations.ProbeListener;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

/**
 *
 * @author pavel.bucek@sun.com
 */

@ManagedObject
public class GlobalStatsProvider {

    private static GlobalStatsProvider INSTANCE = null;

    public static synchronized GlobalStatsProvider getInstance() {
        if(INSTANCE == null)
            INSTANCE = new GlobalStatsProvider();

        return INSTANCE;
    }

    private Set<String> applications;

    private Map<String, ApplicationStatsProvider> applicationStatsProviders;

    private ThreadLocal<List<RuleAccept>> requests = new ThreadLocal<List<RuleAccept>>() {

        @Override
        protected List<RuleAccept> initialValue() {
            return new ArrayList<RuleAccept>();
        }

    };

    private ThreadLocal<String> appName = new ThreadLocal<String>();

    @ManagedAttribute(id="applicationList")
    public Set<String> getApplications() {
        return applications;
    }

    private GlobalStatsProvider() {
        applications = new HashSet<String>();
        applicationStatsProviders = new HashMap<String, ApplicationStatsProvider>();
        // requests = new HashMap<ApplicationInfo, List<RuleAccept>>();
    }

    @ProbeListener("glassfish:jersey:server:ruleAccept")
    public void ruleAccept(
            @ProbeParam("ruleName") String ruleName,
            @ProbeParam("path") CharSequence path,
            @ProbeParam("clazz") Object clazz) {

        RuleAccept ruleAccept = new RuleAccept(ruleName, path, clazz);

        this.requests.get().add(ruleAccept);

        // temporary "fix"; requestEnd listener is not called, don't know why.
        if(ruleName.equals("HttpMethodRule"))
            requestEnd();
    }

    private String getApplicationName(String contextRoot) {

        Habitat habitat = Globals.getDefaultHabitat();

        Domain domain = habitat.getInhabitantByType(Domain.class).get();

        List<Application> applicationList = domain.getApplications().getApplications();

        for(Application app : applicationList) {

            if(app.getContextRoot().equals(contextRoot)) {
                return app.getName();
            }
        }
        
        return null;
    }

    @ProbeListener("glassfish:jersey:server:requestStart")
    public void requestStart(@ProbeParam("contextRoot") String contextRoot) {

        // add application to applications (global "statistics")
        String applicationName = getApplicationName(contextRoot);

        applications.add(applicationName);

        ApplicationStatsProvider applicationStatsProvider;

        if (!applicationStatsProviders.containsKey(applicationName)) {
            //register new ApplicationStatsProvider
            applicationStatsProvider = new ApplicationStatsProvider();
            applicationStatsProviders.put(applicationName, applicationStatsProvider);

            // strange functionality of PluginPoint.APPLICATIONS; it causes to
            // managed object be registered as "server.server.applications.jersey..."
            // and we ant to have it as "server.applications
            // StatsProviderManager.register("glassfish", PluginPoint.APPLICATIONS,
            //        appName + "/jersey/resources", applicationStatsProvider);

            // workaround for ^^^
            StatsProviderManager.register("web-container", PluginPoint.SERVER,
                    "applications/" + applicationName + "/jersey/resources", applicationStatsProvider);

        } else {
            applicationStatsProvider = applicationStatsProviders.get(applicationName);
        }

        this.appName.set(applicationName);
    }

    // it might be better to have requestEnd method instead of checking whether
    // rule name (HttpMethodRule) was reached.
    @ProbeListener("glassfish:jersey:server:requestEnd")
    public void requestEnd() {

        String rootResourceName = null;
        String resourceName = null;

        for (RuleAccept ruleAccept : requests.get()) {
            if(ruleAccept.clazz != null) {
                resourceName = ruleAccept.clazz.getClass().getName();

                if(rootResourceName == null)
                    rootResourceName = resourceName;
            }
        }

        ApplicationStatsProvider asp = this.applicationStatsProviders.get(appName.get());

        asp.rootResourceClassHit(rootResourceName);
        asp.resourceClassHit(resourceName);

        appName.remove();
        requests.remove();
    }
}


// represents accepted rule
class RuleAccept {
    public String ruleName;
    public CharSequence path;
    public Object clazz;

    public RuleAccept() {
    }

    public RuleAccept(String ruleName, CharSequence path, Object clazz) {
        this.ruleName = ruleName;
        this.path = path;
        this.clazz = clazz;
    }
}

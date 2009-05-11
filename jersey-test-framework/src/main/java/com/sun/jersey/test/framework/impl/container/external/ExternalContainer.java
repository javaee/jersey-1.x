/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.jersey.test.framework.impl.container.external;

import com.sun.jersey.test.framework.impl.BasicServletContainer;
import com.sun.jersey.test.framework.impl.Deployable;
import java.net.URI;
import java.util.Map;
/*
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.InstalledLocalContainer;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.configuration.LocalConfiguration;
import org.codehaus.cargo.container.deployable.Deployable;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.ConfigurationFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
 * */

/**
 * A dummy class to represent the use case where the resources are deployed to
 * an external container.
 * @author Naresh (Srinivas.Bhimisetty@Sun.Com)
 */
public class ExternalContainer implements BasicServletContainer, Deployable {

    public void setBaseUri(URI baseUri) {
        //do nothing
    }

    public void start() throws Exception {
        //do nothing - expect the container to be started explicitly
        /*
        GlassFishStandaloneLocalConfiguration localConfiguration = new GlassFishStandaloneLocalConfiguration("c:\\space\\naresh\\workspace\\glassfish");
        InstalledLocalContainer container = new GlassFishInstalledLocalContainer(localConfiguration);

        container.setHome(System.getProperty("glassfish.home"));
        System.out.println("GlassFish Home :: " + container.getHome());

        container.start();
         */
/*
        ConfigurationFactory configurationFactory =
                new DefaultConfigurationFactory();
        LocalConfiguration configuration =
            (LocalConfiguration) configurationFactory.createConfiguration(
            "glassfish2", ContainerType.INSTALLED, ConfigurationType.STANDALONE);

                Deployable war = new DefaultDeployableFactory().createDeployable(
    "glassfish2", "target/simple.war", DeployableType.WAR);
        configuration.addDeployable(war);



        InstalledLocalContainer container =
            (InstalledLocalContainer) new DefaultContainerFactory().createContainer(
            "glassfish2", ContainerType.INSTALLED, configuration);
        container.setHome(System.getProperty("glassfish.home"));

container.start();



        //GlassFishInstalledLocalDeployer deployer = new GlassFishInstalledLocalDeployer(container);
        System.out.println("Container started....");
        Thread.sleep(10000);
        container.stop();
        System.out.println("Container stopped....");
 */
    }

    public void stop() throws Exception {
        //do nothing - expect the conatiner to be stopped explicitly
    }

    public void setInitParams(Map<String, String> initParams) {
        //do nothing
    }

    public void setHttpListenerPort(int httpPort) {
        //do nothing
    }

    public void setContextParams(Map<String, String> contextParams) {
        //do nothing
    }

    public void setServletClass(Class servletClass) {
        //do nothing
    }

    public void setServletListener(String servletListenerClass) {
        //do nothing
    }

    public void setServletPath(String servletPath) {
        //do nothing
    }

    public void setContextPath(String contextPath) {
        //do nothing
    }

}
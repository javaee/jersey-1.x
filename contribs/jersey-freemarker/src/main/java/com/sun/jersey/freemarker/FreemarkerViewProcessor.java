/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.freemarker;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.view.Viewable;
import com.sun.jersey.spi.template.ViewProcessor;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * @author pavel.bucek@oracle.com
 */
public class FreemarkerViewProcessor implements ViewProcessor<String> {

    /**
     * Freemarker templates base path.
     *
     * Mandatory parameter, it has to be set if you plan to use freemarker
     * to generate your views.
     */
    public final static String FREEMARKER_TEMPLATES_BASE_PATH =
            "com.sun.jersey.freemarker.templateBasePath";

    private final Configuration configuration;
    private @Context UriInfo uriInfo;

    private String basePath;

    public FreemarkerViewProcessor(@Context ResourceConfig resourceConfig) {
        configuration = new Configuration();
        configuration.setObjectWrapper(new DefaultObjectWrapper());

        String path = (String)resourceConfig.getProperties().get(
                FREEMARKER_TEMPLATES_BASE_PATH);
        if (path == null)
            this.basePath = "";
        else if (path.charAt(0) == '/') {
            this.basePath = path;
        } else {
            this.basePath = "/" + path;
        }
    }

    @Override
    public String resolve(String path) {

        if (basePath != "")
            path = basePath + path;

        if (uriInfo.getMatchedResources().get(0).getClass().getResource(path) != null) {
            return path;
        }

        if (!path.endsWith(".ftl")) {
            path = path + ".ftl";
            if (uriInfo.getMatchedResources().get(0).getClass().getResource(path) != null) {
                return path;
            }
        }

        return null;
    }

    @Override
    public void writeTo(String resolvedPath, Viewable viewable, OutputStream out) throws IOException {
        // Commit the status and headers to the HttpServletResponse
        out.flush();

        configuration.setClassForTemplateLoading(uriInfo.getMatchedResources().get(0).getClass(), "/");
        final Template template = configuration.getTemplate(resolvedPath);

        try {
            template.process(viewable.getModel(), new OutputStreamWriter(out));
        } catch(TemplateException te) {
            throw new ContainerException(te);
        }
    }
}

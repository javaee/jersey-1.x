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
package com.sun.jersey.impl.container.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Includes a side JSP file for the "it" object.
 *
 * <p>
 * This tag looks for a side JSP file of the given name
 * from the inheritance hierarchy of the it object,
 * and includes the contents of it, just like &lt;jsp:include>.
 *
 * <p>
 * For example, if the "it" object is an instance of the <tt>Foo</tt> class,
 * which looks like the following:
 *
 * <pre>
 * class Foo extends Bar { ... }
 * class Bar extends Zot { ... }
 * </pre>
 *
 * <p>
 * And if you write:
 * <pre><xmp>
 * <st:include page="abc.jsp"/>
 * </xmp></pre>
 * then, it looks for the following files in this order,
 * and includes the first one found.
 * <ol>
 *  <li>a side-file of the Foo class named abc.jsp (/WEB-INF/side-files/Foo/abc.jsp)
 *  <li>a side-file of the Bar class named abc.jsp (/WEB-INF/side-files/Bar/abc.jsp)
 *  <li>a side-file of the Zot class named abc.jsp (/WEB-INF/side-files/Zot/abc.jsp)
 * </ol>
 *
 * @author Kohsuke Kawaguchi
 */
public class Include extends SimpleTagSupport {

    private Object resource;
    private String page;

    /**
     * Specifies the name of the JSP to be included.
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Specifies the object for which JSP will be included.
     */
    public void setResource(Object resource) {
        this.resource = resource;
    }

    private Object getPageObject(String name) {
        return getJspContext().getAttribute(name, PageContext.PAGE_SCOPE);
    }

    public void doTag() throws JspException, IOException {
        Object resource = getJspContext().getAttribute("resource", PageContext.REQUEST_SCOPE);
        final Object oldResource = resource;
        if (this.resource != null) {
            resource = this.resource;
        }

        ServletConfig cfg = (ServletConfig) getPageObject(PageContext.CONFIG);
        ServletContext sc = cfg.getServletContext();

        for (Class c = resource.getClass(); c != Object.class; c = c.getSuperclass()) {
            String name = "/" + c.getName().replace('.', '/') + '/' + page;
            if (sc.getResource(name) != null) {
                // Tomcat returns a RequestDispatcher even if the JSP file doesn't exist.
                // so check if the resource exists first.
                RequestDispatcher disp = sc.getRequestDispatcher(name);
                if (disp != null) {
                    getJspContext().setAttribute("resource", resource, PageContext.REQUEST_SCOPE);
                    try {
                        HttpServletRequest request = (HttpServletRequest) getPageObject(PageContext.REQUEST);
                        disp.include(
                                request,
                                new Wrapper(
                                (HttpServletResponse) getPageObject(PageContext.RESPONSE),
                                new PrintWriter(getJspContext().getOut())));
                    } catch (ServletException e) {
                        throw new JspException(e);
                    } finally {
                        getJspContext().setAttribute("resource", oldResource, PageContext.REQUEST_SCOPE);
                    }
                    return;
                }
            }
        }

        throw new JspException("Unable to find '" + page + "' for " + resource.getClass());
    }
}

class Wrapper extends HttpServletResponseWrapper {

    private final PrintWriter pw;

    public Wrapper(HttpServletResponse httpServletResponse, PrintWriter w) {
        super(httpServletResponse);
        this.pw = w;
    }

    public PrintWriter getWriter() throws IOException {
        return pw;
    }
}
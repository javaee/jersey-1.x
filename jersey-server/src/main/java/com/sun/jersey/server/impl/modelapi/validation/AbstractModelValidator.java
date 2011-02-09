/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011Oracle and/or its affiliates. All rights reserved.
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
/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.php
 * See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.sun.jersey.server.impl.modelapi.validation;

import com.sun.jersey.api.model.AbstractModelComponent;
import com.sun.jersey.api.model.AbstractModelVisitor;
import com.sun.jersey.api.model.ResourceModelIssue;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract resource model validator allows to simply implement
 * validators of resource model. A validator maintains a list
 * of model issues found. User can call validate method to validate
 * several resources by a single validator and collect issues.
 * 
 * @author Jakub.Podlesak@Sun.COM
 */
public abstract class AbstractModelValidator implements AbstractModelVisitor {

    final List<ResourceModelIssue> issueList = new LinkedList<ResourceModelIssue>();

    /**
     * Returns a list of issues found after {@link #validate(com.sun.jersey.api.model.AbstractModelComponent) }
     * method has been invoked
     * @return a non-null list of issues
     */
    public List<ResourceModelIssue> getIssueList() {
        return issueList;
    }

    /**
     * Convenience method to see if there were fatal issues found
     * @return true if there are any fatal issues present in the current issue list
     */
    public boolean fatalIssuesFound() {
        for (ResourceModelIssue issue : getIssueList()) {
            if (issue.isFatal()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all issues from the current issue list. The method could be used
     * to re-use the same validator for another resource model
     */
    public void cleanIssueList() {
        issueList.clear();
    }

    /**
     * The validate method validates a component and adds possible
     * issues found to it's list. The list of issues could be then retreived
     * via getIssueList method
     * 
     * @param component
     */
    public void validate(final AbstractModelComponent component) {
        component.accept(this);
        List<AbstractModelComponent> componentList = component.getComponents();
        if (null != componentList) {
            for (AbstractModelComponent subcomponent : componentList) {
                validate(subcomponent);
            }
        }
    }
}

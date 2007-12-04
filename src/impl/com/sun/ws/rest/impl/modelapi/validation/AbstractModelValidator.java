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
package com.sun.ws.rest.impl.modelapi.validation;

import com.sun.ws.rest.api.model.AbstractModelComponent;
import com.sun.ws.rest.api.model.AbstractModelVisitor;
import com.sun.ws.rest.api.model.ResourceModelIssue;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract resource model validator allows to simply implement
 * validators of resource model. A validator maintains a list
 * of model issues found. User can call validate method to validate
 * several resources by a single validator and collect issues.
 * 
 * @author japod
 */
public abstract class AbstractModelValidator implements AbstractModelVisitor {

    final List<ResourceModelIssue> issueList = new LinkedList<ResourceModelIssue>();

    public List<ResourceModelIssue> getIssueList() {
        return issueList;
    }

    public void cleanIssueList() {
        issueList.removeAll(issueList);
    }

    /**
     * The validate method validates a component and adds possible
     * issues found to it's list. The list of issues could be then retreived
     * via getIssueList method
     * 
     * @param resource
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

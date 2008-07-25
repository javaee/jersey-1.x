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

package com.sun.jersey.impl.modelapi.validation;

import com.sun.jersey.impl.modelapi.validation.BasicValidator;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceLocator;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.api.model.ResourceModelIssue;
import com.sun.jersey.api.model.UriPathValue;
import junit.framework.TestCase;

/**
 *
 * @author japod
 */
public class BasicValidatorTest extends TestCase {
    
    public class TestResource {

        public void getMethod() {
        }
    }
    
    public class TestResourceARM {

        public void getMethod() {
        }

        public String getStringMethod() {
            return "nazdar";
        }
    }
    
    public class TestResourceASRM {
        public void subResMethod() {
        }
    }
    
    public class TestResourceSRL {

        public void subResLocator() {
        }
        
        public Object validSubResLocator() {
            return "nazdar";
        }
    }
    
    
    public BasicValidatorTest(String testName) {
        super(testName);
    }

    /**
     * Test of visit method, of class BasicValidator.
     */
    public void testValidateARM() throws NoSuchMethodException {
        BasicValidator validator = new BasicValidator();
        AbstractResourceMethod arm;

        arm = new AbstractResourceMethod(null, TestResourceARM.class.getMethod("getStringMethod"), "GET");
        assertTrue(validator.getIssueList().isEmpty());
        
        arm = new AbstractResourceMethod(null, TestResourceARM.class.getMethod("getMethod"), "GET");
        validator.validate(arm);
        assertTrue(!validator.getIssueList().isEmpty());
        printIssueList(validator);
    }
    
    public void testValidateSRL() throws NoSuchMethodException {
        BasicValidator validator = new BasicValidator();
        AbstractSubResourceLocator locator;
        locator = new AbstractSubResourceLocator(TestResourceSRL.class.getMethod("subResLocator"), null);
        validator.validate(locator);
        
        int issueCount = validator.getIssueList().size();
        assertTrue(issueCount > 0);
        printIssueList(validator);
        // adding uri template to decrease number of issues
        locator = new AbstractSubResourceLocator(TestResourceSRL.class.getMethod("subResLocator"), new UriPathValue("/test"));
        validator.cleanIssueList();
        validator.validate(locator);
        assertTrue((issueCount - 1) == validator.getIssueList().size());
        locator = new AbstractSubResourceLocator(TestResourceSRL.class.getMethod("validSubResLocator"), new UriPathValue("/test1"));
        validator.cleanIssueList();
        validator.validate(locator);
        assertTrue(0 == validator.getIssueList().size());
    }
    
    public void testValidateASRM() throws NoSuchMethodException {
        BasicValidator validator = new BasicValidator();
        AbstractSubResourceMethod asrm = new AbstractSubResourceMethod(
                null, TestResourceASRM.class.getMethod("subResMethod"), null, "GET");
        validator.validate(asrm);
        int issueCount = validator.getIssueList().size();
        printIssueList(validator);
        assertTrue(issueCount > 0);
        validator.cleanIssueList();
        asrm = new AbstractSubResourceMethod(
                null, TestResourceASRM.class.getMethod("subResMethod"), new UriPathValue("test"), "GET");
        validator.validate(asrm);
        assertTrue((issueCount - 1) == validator.getIssueList().size());
    }
    
    public void testValidateAR() {
        BasicValidator validator = new BasicValidator();
        AbstractResource ar = new AbstractResource(TestResource.class, new UriPathValue(null));
        validator.validate(ar);
        int issueCount = validator.getIssueList().size();
        printIssueList(validator);
        ar = new AbstractResource(TestResource.class, new UriPathValue("/test"));
        validator.cleanIssueList();
        validator.validate(ar);
        assertTrue((issueCount - 1) == validator.getIssueList().size());
    } 

    private static void printIssueList(BasicValidator validator) {
        for (ResourceModelIssue issue : validator.getIssueList()) {
            System.out.println((issue.isFatal() ? "ERROR: " : "WARNING: ") + issue.getMessage());
        }
    }
}

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
package com.sun.jersey.wadl;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Jun 10, 2008<br>
 * 
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @version $Id$
 */
public class Dependency {
    
    private String _groupId;
    private String _artifactId;
    private String _version;
    private String _systemPath;
    
    /**
     * @return the systemPath
     * @author Martin Grotzke
     */
    public String getSystemPath() {
        return _systemPath;
    }

    /**
     * @param systemPath the systemPath to set
     * @author Martin Grotzke
     */
    public void setSystemPath( String systemPath ) {
        _systemPath = systemPath;
    }

    public Dependency() {
    }
    
    public Dependency(String groupId, String artifactId, String version) {
        _groupId = groupId;
        _artifactId = artifactId;
        _version = version;
    }
    /**
     * @return the groupId
     * @author Martin Grotzke
     */
    public String getGroupId() {
        return _groupId;
    }
    /**
     * @param groupId the groupId to set
     * @author Martin Grotzke
     */
    public void setGroupId( String groupId ) {
        _groupId = groupId;
    }
    /**
     * @return the artifactId
     * @author Martin Grotzke
     */
    public String getArtifactId() {
        return _artifactId;
    }
    /**
     * @param artifactId the artifactId to set
     * @author Martin Grotzke
     */
    public void setArtifactId( String artifactId ) {
        _artifactId = artifactId;
    }
    /**
     * @return the version
     * @author Martin Grotzke
     */
    public String getVersion() {
        return _version;
    }
    /**
     * @param version the version to set
     * @author Martin Grotzke
     */
    public void setVersion( String version ) {
        _version = version;
    }

}

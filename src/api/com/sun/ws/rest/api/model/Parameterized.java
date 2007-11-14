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

/*
 * Parameterized.java
 *
 * Created on October 5, 2007, 3:06 PM
 *
 */

package com.sun.ws.rest.api.model;

import java.util.List;

/**
 * Abbstraction for something that has a list of parameters
 */
public interface Parameterized {
    public List<Parameter> getParameters();
}

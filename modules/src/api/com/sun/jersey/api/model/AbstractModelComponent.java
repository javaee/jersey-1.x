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

package com.sun.jersey.api.model;

import java.util.List;

/**
 *
 * @author japod
 */
public interface AbstractModelComponent {
    public void accept(AbstractModelVisitor visitor);
    public List<AbstractModelComponent> getComponents();
}

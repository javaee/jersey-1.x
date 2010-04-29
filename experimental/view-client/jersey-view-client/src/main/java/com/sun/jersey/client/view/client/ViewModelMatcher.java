/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License"). ÊYou
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt. ÊSee the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. ÊIf applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." ÊIf you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above. ÊHowever, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.client.view.client;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.client.view.annotation.Status;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;

/**
 * @author algermissen@acm.org
 * 
 */
public class ViewModelMatcher {

	/**
	 * @param viewModel
	 * @param class1
	 * @param cr
	 * @return
	 */
	public static <T extends Annotation> AnnotatedMethod findMethod(
			ViewModel viewModel, String httpMethod, ClientResponse cr) {

		MethodList methodList = viewModel.getMethodsForHttpMethod(httpMethod);
		for (AnnotatedMethod method : methodList) {
			System.out.println("Checked Method: "
					+ method.getMethod().getName());

			if (!isStatusMatch(method, cr.getStatus())) {
				continue;
			}

			// TODO: ordering issues! This solution here picks the first method
			// that matches, not necessarily the best match
                        MediaType contentType = cr.getType();
                        if (contentType != null) {
                            if (!isMediaTypeMatch(MediaTypes.createMediaTypes(method
                                            .getAnnotation(Consumes.class).value()), contentType)) {
                                    continue;
                            }
                        }

			return method;
		}

		return null;
	}

	private static boolean isStatusMatch(AnnotatedMethod method, int status) {

		if (!method.isAnnotationPresent(Status.class)) {
			return true;
		}
		Status s = method.getAnnotation(Status.class);
		for (int value : s.value()) {
			if (value == status) {
				return true;
			}
		}
		return false;
	}

	private static boolean isMediaTypeMatch(List<MediaType> consumes,
			MediaType contentType) {
		for (MediaType mt : consumes) {
			if (mt.isCompatible(contentType)) {
				return true;
			}
		}
		return false;

	}

}
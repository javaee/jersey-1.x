/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.jersey.oauth.server.resources;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;
import com.sun.jersey.oauth.server.OAuthException;
import com.sun.jersey.oauth.server.OAuthServerRequest;
import com.sun.jersey.oauth.server.spi.OAuthProvider;
import com.sun.jersey.oauth.server.spi.OAuthToken;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Resource handling request token requests.
 *
 * @author Hubert A. Le Van Gong <hubert.levangong at Sun.COM>
 * @author Martin Matula
 */

@Path("/requestToken")
public class RequestTokenRequest {
    private @Context OAuthProvider provider;
    private @Context HttpContext hc;
    /**
     * POST method for creating a request for a Request Token
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/x-www-form-urlencoded")
    public Form postReqTokenRequest() {
        OAuthServerRequest request = new OAuthServerRequest(hc.getRequest());
        OAuthParameters params = new OAuthParameters();
        params.readRequest(request);

        String tok = params.getToken();
        if ((tok != null) && (!tok.contentEquals(""))) {
            throw new OAuthException(Response.Status.BAD_REQUEST, null);
        }

        String consKey = params.getConsumerKey();
        if (consKey == null) {
            throw new OAuthException(Response.Status.BAD_REQUEST, null);
        }

        String consSecret = provider.getConsumerSecret(consKey);
        if (consSecret == null) {
            throw new OAuthException(Response.Status.BAD_REQUEST, null);
        }
        OAuthSecrets secrets = new OAuthSecrets().consumerSecret(consSecret).tokenSecret("");

        boolean sigIsOk = false;
        try {
            sigIsOk = OAuthSignature.verify(request, params, secrets);
        } catch (OAuthSignatureException ex) {
            Logger.getLogger(RequestTokenRequest.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!sigIsOk) {
            throw new OAuthException(Response.Status.BAD_REQUEST, null);
        }

        Map<String, List<String>> parameters = new HashMap<String, List<String>>();
        for (String n : request.getParameterNames()) {
            parameters.put(n, request.getParameterValues(n));
        }

        OAuthToken rt = provider.newRequestToken(consKey, params.getCallback(), parameters);

        Form resp = new Form();
        resp.putSingle(OAuthParameters.TOKEN, rt.getToken());
        resp.putSingle(OAuthParameters.TOKEN_SECRET, rt.getSecret());
        resp.putSingle(OAuthParameters.CALLBACK_CONFIRMED, "true");
        resp.putAll(rt.getCustomParameters());
        return resp;
    }
}

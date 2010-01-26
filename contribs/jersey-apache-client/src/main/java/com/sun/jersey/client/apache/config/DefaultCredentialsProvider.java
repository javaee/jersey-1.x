/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.jersey.client.apache.config;

import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.NTLMScheme;
import org.apache.commons.httpclient.auth.RFC2617Scheme;

/**
 * A simple interactive credentials provider using Swing dialogs to prompt
 * the user for a user name and password.
 *
 * @author jorgeluisw@mac.com
 */
public class DefaultCredentialsProvider implements CredentialsProvider {

    public Credentials getCredentials(AuthScheme scheme,
            String host,
            int port,
            boolean proxy)
            throws CredentialsNotAvailableException {
        if (scheme == null) {
            return null;
        }

        try {
            JTextField userField = new JTextField();
            JPasswordField passwordField = new JPasswordField();
            int response;

            if (scheme instanceof NTLMScheme) {
                JTextField domainField = new JTextField();
                Object[] msg = {
                    host + ":" + port + " requires Windows authentication",
                    "Domain",
                    domainField,
                    "User Name",
                    userField,
                    "Password",
                    passwordField
                };
                response = JOptionPane.showConfirmDialog(null, msg, "Authenticate",
                        JOptionPane.OK_CANCEL_OPTION);

                if ((response == JOptionPane.CANCEL_OPTION) ||
                        (response == JOptionPane.CLOSED_OPTION)) {
                    throw new CredentialsNotAvailableException("User cancled windows authentication.");
                }

                return new NTCredentials(userField.getText(), new String(passwordField.getPassword()),
                        host, domainField.getText());


            } else if (scheme instanceof RFC2617Scheme) {
                Object[] msg = {
                    host + ":" + port + " requires authentication with the realm '" +
                    scheme.getRealm() + "'",
                    "User Name",
                    userField,
                    "Password",
                    passwordField
                };

                response = JOptionPane.showConfirmDialog(null, msg, "Authenticate",
                        JOptionPane.OK_CANCEL_OPTION);

                if ((response == JOptionPane.CANCEL_OPTION) ||
                        (response == JOptionPane.CLOSED_OPTION)) {
                    throw new CredentialsNotAvailableException("User cancled windows authentication.");
                }


                return new UsernamePasswordCredentials(userField.getText(),
                        new String(passwordField.getPassword()));

            } else {

                throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                        scheme.getSchemeName());

            }
        } catch (IOException ioe) {

            throw new CredentialsNotAvailableException(ioe.getMessage(), ioe);

        }
    }
}

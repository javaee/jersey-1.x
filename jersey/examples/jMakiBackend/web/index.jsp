<!--/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */-->
 <%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>jMaki Data Model Example</title>
    </head>
    <body>
    <h1>Web Resources</h1>
    
    <h2>jMaki Data Models</h2>
    <ul>
        <li><a href="webresources/printers/jMakiTable">webresources/printers/jMakiTable</a>
        <li><a href="webresources/printers/jMakiTree">webresources/printers/jMakiTree</a>
    </ul>
    
    <h2>Data Resources</h2>
    <b>webresources/printers/{printerId}</b> methods:
    <ul>
        <li>GET: fetch record for particular printer
        <li>PUT: create/update printer record
        <li>DELETE: delete printer record
    </ul>
    
    </body>
</html>

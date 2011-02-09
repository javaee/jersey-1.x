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
package com.sun.jersey.impl.http.header;

import com.sun.jersey.core.header.LinkHeader;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class LinkTest extends TestCase {

    public LinkTest(String testName) {
        super(testName);
    }

    public void testParse() {
        LinkHeader h = LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>");
        
        assertEquals(URI.create("http://x@host:8080/a/v/%2F?a=c#frag"), h.getUri());
        assertEquals(0, h.getParams().size());
        assertEquals(0, h.getRel().size());
        assertNull(h.getType());

        h = LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;op=GET;x-key=val");
        assertEquals(URI.create("http://x@host:8080/a/v/%2F?a=c#frag"), h.getUri());
        assertEquals(2, h.getParams().size());
        assertEquals(1, h.getRel().size());
        assertTrue(h.getRel().contains("abc"));
        assertEquals("GET", h.getOp());
        assertEquals("val", h.getParams().getFirst("x-key"));

        h = LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=\"abc\";op=\"GET\";x-key=\"val\"");
        assertEquals(URI.create("http://x@host:8080/a/v/%2F?a=c#frag"), h.getUri());
        assertEquals(2, h.getParams().size());
        assertEquals(1, h.getRel().size());
        assertTrue(h.getRel().contains("abc"));
        assertEquals("GET", h.getOp());
        assertEquals("val", h.getParams().getFirst("x-key"));
    }

    public void testParseMultipleRel() {
        LinkHeader h = LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=\"a b c\"");
        assertEquals(3, h.getRel().size());
        assertTrue(h.getRel().contains("a"));
        assertTrue(h.getRel().contains("b"));
        assertTrue(h.getRel().contains("c"));
    }

    public void testParseType() {
        LinkHeader h = LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;type=app/foo;op=GET");
        assertEquals(1, h.getRel().size());
        assertTrue(h.getRel().contains("abc"));
        assertEquals(new MediaType("app", "foo"), h.getType());
        assertEquals("GET", h.getOp());
    }

    public void testParseError() {
        _testParseError("");
        _testParseError("<>");
        _testParseError("<");
        _testParseError(">");
        _testParseError("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;type=app;op=GET");
    }

    private void _testParseError(String h) {
        boolean caught = false;
        try {
            LinkHeader.valueOf(h);
        } catch (IllegalArgumentException ex) {
            caught = true;
        }
        assertTrue(caught);
    }


    public void testToString() {
        LinkHeader lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>"),
                lh);

        lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("abc").
                op("GET").
                parameter("x-key", "val").
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;op=GET;x-key=val"),
                lh);
        
        lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("abc").
                op("GET").
                parameter("x-key", "val").
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=\"abc\";op=\"GET\";x-key=\"val\""),
                lh);
    }

    public void testToStringMultipleRel() {
        LinkHeader lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("a").rel("b").rel("c").
                build();

        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=\"a b c\""),
                lh);
    }

    public void testToStringType() {
        LinkHeader lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("abc").
                type(new MediaType("app", "foo")).
                op("GET").
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;type=app/foo;op=GET"),
                lh);
    }

    public void testToStringParams() {
        LinkHeader lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("abc").
                parameter("hreflang", "en").
                parameter("hreflang", "en-US").
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;hreflang=en;hreflang=en-US"),
                lh);

        lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("abc").
                parameter("title", "abc").
                parameter("title", "abcefg").
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;title=\"abc\""),
                lh);


        lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("abc").
                parameter("title*", "abc").
                parameter("title*", "abcefg").
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;title*=\"abc\";title*=\"abcefg\""),
                lh);

        lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("abc").
                parameter("media", "abc").
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;media=abc"),
                lh);
        lh = LinkHeader.uri(URI.create("http://x@host:8080/a/v/%2F?a=c#frag")).
                rel("abc").
                parameter("media", "a b c").
                build();
        _check(LinkHeader.valueOf("<http://x@host:8080/a/v/%2F?a=c#frag>;rel=abc;media=\"a b c\""),
                lh);
    }

    private void _check(LinkHeader v1, LinkHeader v2) {
        assertEquals(v1.toString(), v2.toString());
    }

}
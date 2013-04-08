/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jersey.samples.mandel

import java.awt.image._

class MandelRenderer(l: Complex, u: Complex, limit: Int, r: WritableRaster) {
    val period = limit / 360.0 * 8.0 * Math.Pi

    val dx = (u - l).re / r.getWidth;

    val dy = (u - l).im / r.getHeight;

    def angle(rate: Int) = period * rate / limit;

    def render() : Unit = {
        render(0, r.getHeight - 1)
    }

    def render(ystart: Int, yend: Int) : Unit = {
        for (val y <- ystart to yend) for (val x <- 0 to r.getWidth - 1) {
            val rate = Mandel.iter(l + Complex(dx * x, dy * y), limit);
            if (rate != limit)
                r.setPixel(x, y, MandelRenderer.rgb(angle(rate)))
        }
    }
}

object MandelRenderer {
    def sample(angle: Double) = Math.sin(angle) * 128.0 + 128.0

    def rgb(angle: Double) = (for (val i <- 0 to 2) yield sample(angle + i * Math.Pi / 3.0)).toArray
}
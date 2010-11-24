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
package mandel

import java.awt.image._

class MandelRenderer(l: Complex, u: Complex, limit: int, r: WritableRaster) {
    val period = limit / 360.0 * 8.0 * Math.Pi

    val dx = (u - l).re / r.getWidth;

    val dy = (u - l).im / r.getHeight;

    def angle(rate: int) = period * rate / limit;

    def render() : unit = {
        render(0, r.getHeight - 1)
    }

    def render(ystart: int, yend: int) : unit = {
        for (val y <- ystart to yend) for (val x <- 0 to r.getWidth - 1) {
            val rate = Mandel.iter(l + new Complex(dx * x, dy * y), limit);
            if (rate != limit)
                r.setPixel(x, y, MandelRenderer.rgb(angle(rate)))
        }
    }
}

object MandelRenderer {
    def sample(angle: double) = Math.sin(angle) * 128.0 + 128.0

    def rgb(angle: double) = (for (val i <- 0 to 2) yield sample(angle + i * Math.Pi / 3.0)).toArray
}
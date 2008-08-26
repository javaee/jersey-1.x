import java.awt.image._
import scala.actors.Actor

package com.sun.jersey.samples.mandel {

class ParallelMandelRenderer(n: int, 
    l: Complex, u: Complex, limit: int, r: WritableRaster) 
        extends MandelRenderer(l, u, limit, r) {

    def intervals(h: int, n: int) = divIntervals(h, n) ++ modIntervals(h, n)

    def divIntervals(h: int, n: int) = for (i <- 0 to h - (h % n) - 1 by h / n) yield (i,  i + h / n - 1)

    def modIntervals(h: int, n: int) = for (i <- h - (h % n) to h - 1) yield (i,  i)

    val yRanges = intervals(r.getHeight, n)

    override def render() : unit = {
        val c = new JobCoordinator(n)
        yRanges foreach ( x => c.job { render(x._1, x._2) } )
        c.waitForCompletion
    } 
}

} // package
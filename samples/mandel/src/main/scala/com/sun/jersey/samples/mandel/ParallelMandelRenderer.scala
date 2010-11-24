package com.sun.jersey.samples.mandel

import java.awt.image._
import scala.actors.Actor

class ParallelMandelRenderer(n: Int,
    l: Complex, u: Complex, limit: Int, r: WritableRaster)
        extends MandelRenderer(l, u, limit, r) {

    def intervals(h: Int, n: Int) = divIntervals(h, n) ++ modIntervals(h, n)

    def divIntervals(h: Int, n: Int) = for (i <- 0 to h - (h % n) - 1 by h / n) yield (i,  i + h / n - 1)

    def modIntervals(h: Int, n: Int) = for (i <- h - (h % n) to h - 1) yield (i,  i)

    val yRanges = intervals(r.getHeight, n)

    override def render() : Unit = {
        val c = new JobCoordinator(n)
        yRanges foreach ( x => c.job { render(x._1, x._2) } )
        c.waitForCompletion
    } 
}
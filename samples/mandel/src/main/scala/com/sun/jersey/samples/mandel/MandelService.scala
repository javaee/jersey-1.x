package com.sun.jersey.samples.mandel

import java.awt.image._
import javax.imageio._
import javax.ws.rs._

@Path("({lx},{ly}),({ux},{uy})")
class MandelService(
        @PathParam("lx") lx: Double,
        @PathParam("ly") ly: Double,
        @PathParam("ux") ux: Double,
        @PathParam("uy") uy: Double,
        @DefaultValue("512") @QueryParam("imageSize") imageSize: Int,
        @DefaultValue("512") @QueryParam("limit") limit: Int,
        @DefaultValue("8") @QueryParam("workers") workers: Int) {

    val lower = Complex(lx, ly);
    val upper = Complex(ux, uy);
    val dx = upper.re - lower.re;
    val dy = upper.im - lower.im;

    val width = if (dx >= dy) imageSize else (dx/dy * imageSize).toInt

    val height = if (dy >= dx) imageSize else (dy/dx * imageSize).toInt
    
    @Produces(Array("image/png"))
    @GET
    def getMandelbrot() = {
        val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        new ParallelMandelRenderer(workers, lower, upper, limit, image.getRaster()).render();
        image
    }
}
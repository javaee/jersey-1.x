package mandel

import java.awt.image._
import javax.imageio._
import javax.ws.rs._

@UriTemplate("({lx},{ly}),({ux},{uy})")
class MandelService(
        @UriParam("lx") lx: double, 
        @UriParam("ly") ly: double,
        @UriParam("ux") ux: double,
        @UriParam("uy") uy: double,
        @DefaultValue("512") @QueryParam("imageSize") imageSize: int,
        @DefaultValue("512") @QueryParam("limit") limit: int,
        @DefaultValue("8") @QueryParam("workers") workers: int) {

    val lower = new Complex(lx, ly);
    val upper = new Complex(ux, uy);
    val dx = upper.re - lower.re;
    val dy = upper.im - lower.im;

    val width : int = if (dx >= dy) imageSize else (dx/dy * imageSize).toInt

    val height : int = if (dy >= dx) imageSize else (dy/dx * imageSize).toInt
    
    @ProduceMime(Array("image/png"))
    @HttpMethod
    def getMandelbrot() = {
        val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        new ParallelMandelRenderer(workers, lower, upper, limit, image.getRaster()).render();
        image
    }
}
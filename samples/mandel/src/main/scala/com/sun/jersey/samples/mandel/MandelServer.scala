package com.sun.jersey.samples.mandel

object MandelServer {
    import com.sun.net.httpserver.HttpServer
    import com.sun.net.httpserver.HttpHandler
    import com.sun.jersey.api.container.httpserver.HttpServerFactory
    import com.sun.jersey.api.container.ContainerFactory
    import com.sun.jersey.api.core.PackagesResourceConfig

    def run() {
        val pkgs: Array[String] = new Array[String](1);
        pkgs(0) = "com.sun.jersey.samples.mandel"

        val handler = ContainerFactory.createContainer(classOf[HttpHandler], new PackagesResourceConfig(pkgs));
        val server = HttpServerFactory.create("http://localhost:9998/mandelbrot", handler);
        server.start();

        println("Server running");
        println("Visit: http://localhost:9998/mandelbrot/(-2.2,-1.2),(0.8,1.2)");
        println("The query parameter 'limit' specifies the limit on number of iterations");
        println("to determine if a point on the complex plain belongs to the mandelbrot set");
        println("The query parameter 'imageSize' specifies the maximum size of the image");
        println("in either the horizontal or virtical direction");
        println("Hit return to stop...");
        System.in.read();
        println("Stopping server");   
        server.stop(0);
        println("Server stopped");
    }

    def main(args: Array[String]) {
        run()
    }
}

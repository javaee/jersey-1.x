package mandel

object MandelServer {
    import com.sun.ws.rest.api.container.ContainerFactory
    import com.sun.net.httpserver.HttpHandler
    import com.sun.net.httpserver.HttpServer
    import java.net.InetSocketAddress
    import java.util.HashSet

    def run() {
        val resources = new HashSet()
        resources.add(classOf[MandelService])
        val handler =
            ContainerFactory.createContainer(classOf[HttpHandler], resources).
        asInstanceOf[HttpHandler];
        val server = HttpServer.create(
            new InetSocketAddress(9998), 0);
        server.createContext("/mandelbrot", handler);
        server.setExecutor(null);
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
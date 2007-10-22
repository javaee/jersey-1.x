package mandel

import java.awt.image._
import java.io._
import javax.imageio._
import javax.ws.rs._
import javax.ws.rs.core._
import javax.ws.rs.ext._

class RenderedImageProvider extends EntityProvider {
    def supports(t: Class) : boolean = {
        classOf[RenderedImage].isAssignableFrom(t)
    }

    def readFrom(t: Class,
            mediaType: MediaType, 
            httpHeaders: MultivaluedMap, 
            entityStream: InputStream) : RenderedImage = {
        // Not implemented
        null
    }

    def writeTo(t: Any,
            mediaType: MediaType,
            httpHeaders: MultivaluedMap,
            entityStream: OutputStream) : unit = {
        val formatName = RenderedImageProvider.formatName(mediaType)
        if (formatName == null) throw new IOException("Media type " + 
            mediaType + "not supported")
        ImageIO.write(t.asInstanceOf[RenderedImage], formatName, entityStream)
    }

}

object RenderedImageProvider {
    def formatName(t: MediaType) : String = {
        formatName(t.toString())
    }

    def formatName(t: String) : String = {
        val i = ImageIO.getImageWritersByMIMEType(t)
        if (!i.hasNext()) return null

        i.next.asInstanceOf[ImageWriter].getOriginatingProvider.getFormatNames()(0)
    }

    def isSupported(t: MediaType) =  ImageIO.getImageWritersByMIMEType(t.toString()).hasNext()
}
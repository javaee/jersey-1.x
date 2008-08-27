import java.awt.image._
import java.io._
import javax.imageio._
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import javax.ws.rs._
import javax.ws.rs.core._
import javax.ws.rs.ext._

package com.sun.jersey.samples.mandel {

@Provider
class RenderedImageProvider extends MessageBodyWriter[RenderedImage] {
    def isWriteable(c: Class[_], gt: Type, annotations: Array[Annotation], mediaType: MediaType) : boolean = {
        classOf[RenderedImage].isAssignableFrom(c)
    }

    def writeTo(t: RenderedImage,
            c: Class[_],
            gt: Type,
            annotations: Array[Annotation],
            mediaType: MediaType,
            httpHeaders: MultivaluedMap[String, Object],
            entityStream: OutputStream) : unit = {
        val formatName = RenderedImageProvider.formatName(mediaType)
        if (formatName == null) throw new IOException("Media type " + 
            mediaType + "not supported")
        ImageIO.write(t, formatName, entityStream)
    }

    def getSize(t: RenderedImage, c: Class[_], gt: Type, annotations: Array[Annotation], mediaType: MediaType) : long = {
        -1
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

    def isSupported(t: MediaType) = ImageIO.getImageWritersByMIMEType(t.toString()).hasNext()
}

} // package
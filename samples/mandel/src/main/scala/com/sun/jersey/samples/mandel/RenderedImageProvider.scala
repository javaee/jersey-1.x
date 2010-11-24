package com.sun.jersey.samples.mandel

import java.awt.image._
import java.io._
import javax.imageio._
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import javax.ws.rs._
import javax.ws.rs.core._
import javax.ws.rs.ext._

@Produces(Array("image/*"))
@Provider
class RenderedImageProvider extends MessageBodyWriter[RenderedImage] {
    def isWriteable(c: Class[_], gt: Type, annotations: Array[Annotation], mediaType: MediaType) : Boolean = {
        classOf[RenderedImage].isAssignableFrom(c)
    }

    def writeTo(t: RenderedImage,
            c: Class[_],
            gt: Type,
            annotations: Array[Annotation],
            mediaType: MediaType,
            httpHeaders: MultivaluedMap[String, Object],
            entityStream: OutputStream) : Unit = {
        val formatName = RenderedImageProvider.formatName(mediaType)
        if (formatName == null) throw new IOException("Media type " + 
            mediaType + "not supported")
        ImageIO.write(t, formatName, entityStream)
    }

    def getSize(t: RenderedImage, c: Class[_], gt: Type, annotations: Array[Annotation], mediaType: MediaType) : Long = {
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

        i.next.getOriginatingProvider.getFormatNames()(0)
    }

    def isSupported(t: MediaType) = ImageIO.getImageWritersByMIMEType(t.toString()).hasNext()
}
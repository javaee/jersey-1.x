package com.sun.jersey.lift

import javax.servlet.http.HttpServletRequest

/**
 * @version $Revision : 1.1 $
 */

object Requests {
  val holder = new ThreadLocal[HttpServletRequest]() {}

  def request: HttpServletRequest = holder.get()

  /**
   * Sets the current request
   */
  def set(request: HttpServletRequest) {
    holder.set(request)
  }


  /**
   * Returns the URI which might be prefixed by the local context otherwise the same URI is returned
   */
  def uri(localUri: String): String = {
    val r = request
    if (r != null) {
      val contextPath = r.getContextPath
      if (contextPath != null && contextPath.length > 0 && contextPath != "/") {
        return contextPath + localUri
      }
    }
    localUri
  }
}
package com.sun.jersey.lift


import javax.servlet.http.HttpServletRequest
import _root_.net.liftweb.http.{Req, S, RequestVar}
import _root_.net.liftweb.util.{Log, Full}
/**
 * A request scoped variable for accessing the current resource bean when
 * using implicit views
 *
 * @version $Revision : 1.1 $
 */
object ResourceBean {
  def get: AnyRef = {
    val request = Requests.request
    if (request != null) {
      request.getAttribute("it")
    }
    else {
      Log.warn("No HttpServletRequest object registered with Requests.set()!")
      null
    }
  }

  /**
   * Lets pass in the http request as we tend to be invoked outside of the usual Lift
   * processing lifecycle so the Lift Req thread locals are typically not registered here
   */
  def set(request: HttpServletRequest, newValue: AnyRef) {
    Requests.set(request)
    request.setAttribute("it", newValue)
  }
}
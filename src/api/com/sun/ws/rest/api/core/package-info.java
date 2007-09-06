/**
 * Low-level interfaces and annotations used to create RESTful service
 * resources. E.g.:
<pre>
&#064;URITemplate("widgets/{widgetid}")
public class WidgetResource extends WebResource {

  public void handleRequest(HTTPRequest request, HTTPResponse response) {
    if (request.getHttpMethod().equals("GET")) {
      String replyStr = "&lt;widget id='"+
        request.getURIParameters().get("widgetId").get(0).getValue()+"'/&gt;";
      StringRepresentation reply = new StringRepresentation(replyStr,
        "application/widgets+xml");
      response.setRepresentation(reply);
    }
    else ...
  }

}
</pre>
 */
package com.sun.ws.rest.api.core;
/**
 * High-level interfaces and annotations used to create RESTful service 
 * resources. E.g.:
<pre>
&#064;UriTemplate("widgets/{widgetid}")
&#064;ConsumeMime("application/widgets+xml")
&#064;ProduceMime("application/widgets+xml")
public class WidgetResource {

  &#064;HttpMethod(GET)
  public Representation getWidget(&#064;UriParam("widgetid") String id) {
    String replyStr = getWidgetAsXml(id);
    StringRepresentation reply = new StringRepresentation(replyStr,
      "application/widgets+xml");
    return reply;
  }
  
  &#064;HttpMethod(PUT)
  public void updateWidget(&#064;UriParam("widgetid") String id,
    Representation&lt;Source&gt; update) {
    updateWidgetFromXml(id, update);
  }
  
  ...
}
</pre>
 */
package com.sun.jersey.api;
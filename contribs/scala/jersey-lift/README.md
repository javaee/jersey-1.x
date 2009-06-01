# Jersey support

This module provides suppport for Lift templates within JAXRS and Jersey in particular.

Various options are available for using Lift templates within resource beans

## Returning a Lift template from a resource bean method

You can return a Scala *NodeSeq* from a resource method which will then be transformed to
HTML/XHTML/XML via the lift template.

### Example

See the [ResourceReturningTemplateView.scala](http://github.com/jstrachan/liftweb/blob/master/lift-jersey-test/src/main/scala/net/liftweb/jersey/test/resources/ResourceReturningTemplateView.scala) example resource bean which
the *view()* method returns markup defining a Lift template

## Using Jersey implicit views

Using this approach you annotate your resource bean with @ImplicitProduces (you can include MIME types too)
and then Jersey will find your Lift template in package/className/index.html.

For example for a bean called com.acme.CheeseResource then you should create a template called
com/acme/CheeseResource.html

You can have multiple templates in the directory which are then addressable using the URI of the
resource postfixed by the template name (you can leave off the .html extension)

###ÊExample

The [ResourceWithImplicitView.scala](http://github.com/jstrachan/liftweb/blob/master/lift-jersey-test/src/main/scala/net/liftweb/jersey/test/resources/ResourceWithImplicitView.scala) example resource bean is annotated with @ImplicitProduces
and the *view()* method returns itself.

Then the [index.html](http://github.com/jstrachan/liftweb/blob/master/lift-jersey-test/src/main/webapp/net/liftweb/jersey/test/resources/ResourceWithImplicitView/index.html) template file is a Lift template which is rendered properly via Lift


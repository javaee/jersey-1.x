package net.liftweb.jersey.test


import _root_.org.specs._
import _root_.net.sourceforge.jwebunit.junit.WebTester
import org.junit.runner.RunWith
import runner.{JUnitSuiteRunner, JUnit4, ConsoleRunner}

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitSuiteRunner])
class EmbeddedTemplateTest extends JUnit4(EmbeddedTemplate)
object EmbeddedTemplateRunner extends ConsoleRunner(EmbeddedTemplate)

object EmbeddedTemplate extends Specification {
  JettyTestServer.start()

  "lift jersey" should {
    "render lift template inside resource method" >> {
      JettyTestServer.browse(
        "/resourceReturningTemplateView", {
          p =>
                  logger.debug("Response >>>>" + p.getPageSource() + "<<<<")
                  p.assertElementPresentByXPath("/html/body//h2[@class='mytitle']")
        })
    }
  }

  //  JettyTestServer.stop()
}
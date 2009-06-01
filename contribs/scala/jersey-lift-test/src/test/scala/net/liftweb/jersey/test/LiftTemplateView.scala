package net.liftweb.jersey.test


import _root_.org.specs._
import _root_.net.sourceforge.jwebunit.junit.WebTester
import org.junit.runner.RunWith
import runner.{JUnitSuiteRunner, JUnit4, ConsoleRunner}

/**
 * @version $Revision : 1.1 $
 */
@RunWith(classOf[JUnitSuiteRunner])
class LiftTemplateViewTest extends JUnit4(LiftTemplateView)
object LiftTemplateViewRunner extends ConsoleRunner(LiftTemplateView)

object LiftTemplateView extends Specification {
  JettyTestServer.start()

  "lift jersey" should {
    "render lift template using implicit view in separate html file" >> {
      JettyTestServer.browse(
        "/resourceWithImplicitView", {
          p =>
                  Console.println("Response >>>>" + p.getPageSource() + "<<<<")
                  logger.debug("Response >>>>" + p.getPageSource() + "<<<<")
                  p.assertElementPresentByXPath("/html/body//h2[@class='mytitle']")
        })
    }
  }

  //  JettyTestServer.stop()
}
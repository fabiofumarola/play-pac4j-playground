package controllers

import javax.inject.{Inject, _}

import org.pac4j.core.config.Config
import org.pac4j.core.profile._
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala._
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._

import scala.collection.JavaConverters._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (
  val cc: ControllerComponents,
  val config: Config,
  val playSessionStore: PlaySessionStore,
  val actionBuilder: DefaultActionBuilder
) extends AbstractController(cc) with Security[CommonProfile] {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  //this method can be moved to a common trait
  private def getProfiles(implicit request: RequestHeader): List[CommonProfile] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profiles = profileManager.getAll(true)
    asScalaBuffer(profiles).toList
  }

  def basicSecured() = Secure("DirectBasicAuthClient") { profiles =>
    actionBuilder { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def basicAuthAlternative = actionBuilder { request =>
    val profiles = getProfiles(request)
    Ok(views.html.protectedIndex(profiles))
  }

}

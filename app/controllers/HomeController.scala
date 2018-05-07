package controllers

import javax.inject.{Inject, _}
import org.pac4j.core.config.Config
import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.profile._
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.profile.JwtGenerator
import org.pac4j.play.PlayWebContext
import org.pac4j.play.scala._
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc._
import org.pac4j.core.util.CommonHelper
import play.api.Configuration

import scala.collection.JavaConverters._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() (
  val cc: ControllerComponents,
  val config: Config,
  val conf: Configuration,
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
    val webContext = new PlayWebContext(request, playSessionStore)
    val sessionStore = webContext.getSessionStore.asInstanceOf[SessionStore[PlayWebContext]]
    val sessionId = sessionStore.getOrCreateSessionId(webContext)
    val csrfToken = sessionStore.get(webContext, Pac4jConstants.CSRF_TOKEN).asInstanceOf[String]
    Ok(views.html.index(getProfiles(request), sessionId, csrfToken))
  }

  def loginHttpForm = Secure("IndirectBasicAuthClient") { profiles =>
    actionBuilder { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def secured() = Secure("DirectBasicAuthClient") { profiles =>
    actionBuilder { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  /**
    * check the application.conf to find the interceptor for the client to be used
    * @return
    */
  def securedFilters = actionBuilder { request =>
    val profiles = getProfiles(request)
    Ok(views.html.protectedIndex(profiles))
  }

  def jwtGenerate() = Secure("IndirectBasicAuthClient") { profiles =>
    actionBuilder { request =>
      val generator = new JwtGenerator[CommonProfile](
        new SecretSignatureConfiguration(conf.get[String]("pac4j.jwt_secret")))
      var token = ""
      if (CommonHelper.isNotEmpty(profiles.asJava)){
        token = generator.generate(profiles.asJava.get(0))
      }
      Ok(views.html.jwt.render(token))
    }
  }

  def securedJwt() = Secure("ParameterClient") {profiles =>
    actionBuilder { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }


  //this method can be moved to a common trait
  private def getProfiles(implicit request: RequestHeader): List[CommonProfile] = {
    val webContext = new PlayWebContext(request, playSessionStore)
    val profileManager = new ProfileManager[CommonProfile](webContext)
    val profiles = profileManager.getAll(true)
    asScalaBuffer(profiles).toList
  }
}

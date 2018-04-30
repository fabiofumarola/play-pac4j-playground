package modules

import com.google.inject.{AbstractModule, Provides}
import controllers.UnauthorizedHttpActionAdapter
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.http.client.direct.{DirectBasicAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.IndirectBasicAuthClient
import org.pac4j.http.credentials.authenticator.test.{SimpleTestTokenAuthenticator, SimpleTestUsernamePasswordAuthenticator}
import org.pac4j.play.{CallbackController, LogoutController}
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import play.api.{Configuration, Environment}

class SecurityModule(env: Environment, conf: Configuration) extends AbstractModule {

  val baseUrl = conf.get[String]("baseUrl")

  override def configure(): Unit = {
    //The PlayCacheSessionStore is defined as the implementation for the session store: profiles will be saved in the Play Cache.
    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])

    // callback
    val callbackController = new CallbackController()
    callbackController.setDefaultUrl("/?defaulturlafterlogout")
    callbackController.setMultiProfile(true)
    bind(classOf[CallbackController]).toInstance(callbackController)

    // logout
    val logoutController = new LogoutController()
    logoutController.setDefaultUrl("/")
    bind(classOf[LogoutController]).toInstance(logoutController)
  }

  @Provides
  def directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator)

  @Provides
  def provideIndirectBasicAuthClient: IndirectBasicAuthClient = new IndirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator())

  @Provides
  def provideParameterClient: ParameterClient = {
    //authenticate using a simple not empty token
    val client = new ParameterClient("token", new SimpleTestTokenAuthenticator)
    client.setSupportGetRequest(true)
    client.setSupportPostRequest(false)
    client
  }

  @Provides
  def providesConfig(
    directBasicAuthClient: DirectBasicAuthClient,
    indirectBasicAuthClient: IndirectBasicAuthClient,
    parameterClient: ParameterClient,
  ): Config = {
    //  1. define the client
    val clients = new Clients(baseUrl + "/callback", directBasicAuthClient, indirectBasicAuthClient, parameterClient)

    //add the config for your clients
    val config = new Config(clients)
    config.setHttpActionAdapter(new UnauthorizedHttpActionAdapter())
    config
  }
}

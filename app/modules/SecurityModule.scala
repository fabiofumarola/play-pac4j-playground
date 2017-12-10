package modules

import com.google.inject.{AbstractModule, Provides}
import controllers.UnauthorizedHttpActionAdapter
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.http.client.direct.DirectBasicAuthClient
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import play.api.{Configuration, Environment}

class SecurityModule(env: Environment, conf: Configuration) extends AbstractModule {

  override def configure(): Unit = {
    //The PlayCacheSessionStore is defined as the implementation for the session store: profiles will be saved in the Play Cache.
    bind(classOf[PlaySessionStore]).to(classOf[PlayCacheSessionStore])
  }

  @Provides
  def directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator)


  @Provides
  def providesConfig(
    directBasicAuthClient: DirectBasicAuthClient
  ): Config = {
    //  1. define the client
    val clients = new Clients(directBasicAuthClient)

    //add the config for your clients
    val config = new Config(clients)
    config.setHttpActionAdapter(new UnauthorizedHttpActionAdapter())

    config
  }
}

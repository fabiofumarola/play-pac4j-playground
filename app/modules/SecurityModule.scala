package modules

import java.time.Duration

import com.google.inject.{AbstractModule, Provides}
import controllers.UnauthorizedHttpActionAdapter
import org.ldaptive.auth.{Authenticator, FormatDnResolver, PooledBindAuthenticationHandler, SearchDnResolver}
import org.ldaptive.pool._
import org.ldaptive.ssl.SslConfig
import org.ldaptive.{BindConnectionInitializer, ConnectionConfig, Credential, DefaultConnectionFactory}
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.http.client.direct.{DirectBasicAuthClient, ParameterClient}
import org.pac4j.http.client.indirect.IndirectBasicAuthClient
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.ldap.profile.service.LdapProfileService
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

  private def getJwtAuthenticator = {
    val jwtAuthenticator = new JwtAuthenticator()
    jwtAuthenticator.addSignatureConfiguration(
      new SecretSignatureConfiguration(conf.get[String]("pac4j.jwt_secret"))
    )
    jwtAuthenticator
  }

  private def getLdapAuthenticator() = {
    val connectionConfig = new ConnectionConfig()
    connectionConfig.setConnectTimeout(
      Duration.ofMillis(conf.get[Long]("pac4j.ldap.conn_timeout")))
    connectionConfig.setResponseTimeout(
      Duration.ofMillis(conf.get[Long]("pac4j.ldap.resp_timeout")))
    connectionConfig.setLdapUrl(conf.get[String]("pac4j.ldap.url"))

    connectionConfig.setConnectionInitializer(
      new BindConnectionInitializer(
        conf.get[String]("pac4j.ldap.bind_dn"),
        new Credential(conf.get[String]("pac4j.ldap.bind_pwd"))
      )
    )

    connectionConfig.setUseSSL(true) //TODO Shall we keep SSL mandatory
    val sslConfig = new SslConfig()
    sslConfig.setTrustManagers() //TODO no more certificate validation, shall we keep it in this way?
    connectionConfig.setSslConfig(sslConfig)

    val connectionFactory = new DefaultConnectionFactory()
    connectionFactory.setConnectionConfig(connectionConfig)
    val poolConfig = new PoolConfig()
    poolConfig.setMinPoolSize(1)
    poolConfig.setMaxPoolSize(2)
    poolConfig.setValidateOnCheckIn(true)
    poolConfig.setValidateOnCheckOut(true)
    poolConfig.setValidatePeriodically(false)

    val searchValidator = new SearchValidator
    val pruneStrategy = new IdlePruneStrategy
    val connectionPool = new BlockingConnectionPool
    connectionPool.setPoolConfig(poolConfig)
    connectionPool.setBlockWaitTime(Duration.ofMillis(1000))
    connectionPool.setValidator(searchValidator)
    connectionPool.setPruneStrategy(pruneStrategy)
    connectionPool.setConnectionFactory(connectionFactory)
    connectionPool.initialize()
    val pooledConnectionFactory = new PooledConnectionFactory
    pooledConnectionFactory.setConnectionPool(connectionPool)

    val pooledBindHandler = new PooledBindAuthenticationHandler()
    pooledBindHandler.setConnectionFactory(pooledConnectionFactory)

    val dnResolver = new SearchDnResolver(connectionFactory)
    dnResolver.setBaseDn(conf.get[String]("pac4j.ldap.base_user_dn"))
    dnResolver.setUserFilter(
      s"(${conf.get[String]("pac4j.ldap.login_attribute")}={user})")

    val authenticator = new Authenticator()
    authenticator.setDnResolver(dnResolver)
    authenticator.setAuthenticationHandler(pooledBindHandler)

    val ldapProfileService = new LdapProfileService(connectionFactory, authenticator,
      conf.get[String]("pac4j.ldap.base_user_dn")
    )
    ldapProfileService.setAttributes("memberOf")
    ldapProfileService.setUsernameAttribute(conf.get[String]("pac4j.ldap.username_attribute"))

    ldapProfileService
  }



  //now we use ldap
  @Provides
  def directBasicAuthClient =
    new DirectBasicAuthClient(getLdapAuthenticator())


  @Provides
  def provideIndirectBasicAuthClient: IndirectBasicAuthClient =
    new IndirectBasicAuthClient(getLdapAuthenticator())

  @Provides
  def provideParameterClient: ParameterClient = {
    //authenticate using a simple not empty token
    val client = new ParameterClient("token", getJwtAuthenticator)
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

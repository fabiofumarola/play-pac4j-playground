@@@ index

* [Basic Authentication](basic_auth.md)

@@@

# Pac4j Play Integration

The goal of this project is to create a well documented playground for pac4j authentication and authorization with Play.

The main concepts in pac4j are:

- **a client**: that represents an authentication mechanism. It performs the login process and returns a user profile.
- **an authenticator**: is required by HTTP clients to validate provided credentials.
- **an authorizer**: that checks if the given profile has the rights to access a requested resource.
- **a matcher**: defines a (regular) expression that is applied on security filters
- **a config**: which defines the security configurations via clients, authorizers and matchers

## 1. Clients

Clients can be categorized into:
1. direct client: such as web services o HTTP clients, which negotiate the access directly.
2. indirect client: that uses redirects to a third party service to perform the login (e.g. facebook, google, twitter)
More details about them can be found [here](http://www.pac4j.org/docs/clients.html#1-direct-vs-indirect-clients)

The supported clients are listed [here](http://www.pac4j.org/docs/clients.html).

While most clients are self-sufficient, the HTTP clients require defining an Authenticator to handle the credentials validation.

In this playground we are interested in:

1. basic auth (using both indirect ([IndirectBasicAuthClient](https://github.com/pac4j/pac4j/blob/master/pac4j-http/src/main/java/org/pac4j/http/client/indirect/IndirectBasicAuthClient.java)) and direct ([DirectBasicAuthClient](https://github.com/pac4j/pac4j/blob/master/pac4j-http/src/main/java/org/pac4j/http/client/direct/DirectBasicAuthClient.java)) clients).
2. cookie client: [CookieClient](https://github.com/pac4j/pac4j/blob/master/pac4j-http/src/main/java/org/pac4j/http/client/direct/CookieClient.java) value sent as a cookie
3. header client: [HeaderClient](https://github.com/pac4j/pac4j/blob/master/pac4j-http/src/main/java/org/pac4j/http/client/direct/HeaderClient.java) value sent as a HTTP header
4. parameter client: [ParameterClient](https://github.com/pac4j/pac4j/blob/master/pac4j-http/src/main/java/org/pac4j/http/client/direct/ParameterClient.java) value sent as a HTTP parameter

Clients (like Authorizers) are generally defined in a [security configuration](http://www.pac4j.org/docs/config.html).

## 2. Authenticator

It is used by **Clients** (see above) to validate credentials and create a user profile.
The `Authenticator` interface has only one method

```scala
def validate(c: Credentials, context: WebContext): Unit {

}
```

Credentials provided via clients can be of two types:

- username/password [UsernamePasswordCredentials](https://github.com/pac4j/pac4j/blob/master/pac4j-core/src/main/java/org/pac4j/core/credentials/UsernamePasswordCredentials.java)
- tokens [Token Credentials](https://github.com/pac4j/pac4j/blob/master/pac4j-core/src/main/java/org/pac4j/core/credentials/TokenCredentials.java) such as JWT

We can define different [Authenticators](http://www.pac4j.org/docs/authenticators.html), but we are interested on:
- LDAP
- JWT
- REST API

### Recommendations:

1. use a [LocalCachingAuthenticator](https://github.com/pac4j/pac4j/blob/master/pac4j-core/src/main/java/org/pac4j/core/credentials/authenticator/LocalCachingAuthenticator.java) to cache the requests made to the identity provider. It uses guava but it can be modified to use another store.


## 3. Authorizer

It checks if a user profile extracted by a client has the authorization to access the requested resource.

Urls can be protected via:

1. Secure annotation on controllers methods

```scala

  def basicSecured() = Secure("DirectBasicAuthClient") { profiles =>
    actionBuilder { request =>
      Ok(views.html.protectedpage(profiles))
    }
  }

```

2. Security filters defined in the `application.conf` file

```hocon 

pac4j.security {
  rules = [
    {"/profile" = {
      authorizers = "_authenticated_"
      clients = "DirectBasicAuthClient"
    }}
  ]
}

```

# Other Clients

In the @ref[basic auth](./basic_auth.md) we used an authentication header to display a secured route, in the following we are going to see:

1. how to use `IndirectBasicAuthClient` to perform the login using the browser
2. how to use `ParameterClient` to perform the login via a param `?token`
3. how to perform the logout.

> Note that indirect clients are for UI authentication while direct for we service authentication

## Indirect Basic Auth Client

It can be used to perform authentication using the included browser login form.
The process flows as follow:

1) The originally requested URL is saved in session (by the “security filter”)
2) The user is redirected to the identity provider (by the “security filter”)
3) Authentication happens at the identity provider (or locally for the FormClient and the IndirectBasicAuthClient)
4) The user is redirected back to the callback endpoint/URL (“callback filter”)
5) The user is redirected to the originally requested URL (by the “callback filter”)

The parameter client can be used to perform login via a token (provided by a valid authorizer such as JWT)

## Steps

Thus, in order to add it we need to setup an `IndirectBasicAuthClient`, add the need callbacks to handle login and logout, 
and update the client definition to handle the new login client.

### Updates to the Security Module

1. to update the `configure` adding a controller for callback and logout 
2. Add to the security module an instance of `IndirectBasicAuthClient`, 
3. update the `Clients` with the new client configuration plus the url to handle the callback,
4. add a `callback` filter to handle the process
5. add an 'provideParameterClient' to handle token access

@@snip [SecurityModule.scala](../modules/SecurityModule.scala)

As you can note we are still using test authorizers like:
1. same username and password, or
2. non empty token

### 2. Update the controller

In the home controller we add a method definition for the new route (remember to add the route in the routes file).

```play2htmlrouting

GET     /browserform                controllers.HomeController.browserForm
GET     /paramsecured               controllers.HomeController.paramSecured
```

add the two routes for the browser form and params

```scala

  def browserForm = Secure("IndirectBasicAuthClient") { profiles =>
    actionBuilder { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }

  def paramSecured() = Secure("ParameterClient") {profiles =>
    actionBuilder { request =>
      Ok(views.html.protectedIndex(profiles))
    }
  }
```

#### Other Clients

Pac4j supports other indirect clients like facebook, twitter, google app engine but we are not going to cover them here.

# Basic Authentication

## Security Module

In the package `app` create a folder named `module` and place the class `SecurityModule` that extends `AbstracModule`.
This class uses [Google dependency injection](https://github.com/google/guice) and defines the client to be used which is
`DirectBasicAuthClient`. 

Both the clients uses `SimpleTestUsernamePasswordAuthenticator` as authenticator, that requires username equal to the password. 
In the method `provideConfig` the two clients are bind to an instance of `org.pac4j.core.config.Config`.

@@snip [SecurityModule.scala](../modules/SecurityModule.scala)

Finally, this module is enabled by adding the following line in the file `application.conf`.

```hocon

play.modules.enabled += "modules.SecurityModule"

```

## Securing a Controller

Now it is time to try the security module by securing a route in a controller. This is done decorating a method with:

```scala

 Secure("DirectBasicAuthClient") { profiles =>
    //method logic and result
 }
```

A complete example is provide in the snippet below, where you can see how the method `basicSecured` is secured using the `DirectBasicAuthClient`.

@@snip [HomeController.scala](../controllers/HomeController.scala)

### Using Security Filters

Alternatively, we can secure a set of routes via `SecurityFilters`. In order to do that we need to:

1. define a class `Filters` in the package `app\filters`

@@snip [Filters.scala](../filters/Filters.scala)

2. load the filter in the `application.conf`

```hocon

play.http.filters = "filters.Filters"
```

3. add the filters rules in the `application.conf`. Here we are securing the path `/basicalternative`, which matches to the method `basicAuthAlternative`.

```hocon

pac4j.security {
  rules = [
    {"/basicalternative" = {
      authorizers = "_authenticated_"
      clients = "DirectBasicAuthClient"
    }}
  ]
}
```

@@@ note

We can protect multiple routes by using wildcards and regular expression.

```hocon

pac4j.security {
  rules = [
    {"/filter/basicauth.*" = {
      authorizers = "_authenticated_"
      clients = "IndirectBasicAuthClient"
    }}
    {"/filter/.*" = {
      authorizers = "_authenticated_"
    }}
  ]
}

```

@@@

This is what we need to setup basic authentication. 
Now we can start the application with `sbt run` and then do the following curl to login with basic auth using admin/admin as credentials.

```bash

curl -X GET "http://localhost:9000/basicsecured" -H  "accept: application/json" -H  "Authorization: Basic YWRtaW46YWRtaW4="

```

In the following we will see:

1. how to use `IndirectBasicAuthClient` to perform the login using the broser
2. how to use `ParameterClient` to perform the login via a param `?user`
3. how to perform the logout.








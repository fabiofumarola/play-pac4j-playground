# Authorizers

In this page we are going to replace dummy username/password and non empty token authorizers with a valid LDAP and JWT authorizers.
In order to test the application we need to run [ldap test server]()https://github.com/rroemhild/docker-test-openldap) as local service via docker

```
docker run --privileged -d -p 389:389 -p 636:636 --name ldap rroemhild/test-openldap
```

## LDAP

Add the configuration for ldap in the SecurityModule:

@@snip [HomeController.scala](../modules/SecurityModule.scala)

In particular, check the `getLdapAuthenticator` method and the `directBasicAuthClient` that now uses ldap as provider for authentication.

And then add the new routes in the `HomeController`

@@snip [HomeController.scala](../controllers/HomeController.scala)
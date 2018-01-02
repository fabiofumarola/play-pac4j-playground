# Common Helpers

## Custom Error Handler

[This](https://www.playframework.com/documentation/2.6.x/ScalaErrorHandling) is used to detect and handle message errors for client and server errors.
Play will in many circumstances automatically handle server errors - if your action code throws an exception, 
Play will catch this and generate a server error page to send to the client.
In the snippet below you can see how it is setup for this application.

@@snip[CustomErrorHandler.scala](./../../app/controllers/CustomErrorHandler.scala)

The error handler is activate adding the following configuration to your `application.conf`.

```hocon

play.http.errorHandler = "CustomErrorHandler"
```

Finally, in the package `views` we added the page `error500.scala.html` that display the errors to the client.

@@snip[error500.scala.html](./../views/error500.scala.html)


## Unauthorized Http Action Adapter

This class is used to manage authorization errors. It displays a 401 or a 403 page basing on the error code.

@@snip[UnauthorizedHttpActionAdapter.scala](./../controllers/UnauthorizedHttpActionAdapter.scala)

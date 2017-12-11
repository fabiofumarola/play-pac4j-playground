package controllers

import javax.inject.Singleton

import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.mvc.RequestHeader
import play.api.mvc.Results._

import scala.concurrent.Future

@Singleton
class CustomErrorHandler extends HttpErrorHandler {

  val log = Logger(this.getClass)

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful {
      log.error(s"Error occurred ${exception.getMessage}", exception)
      InternalServerError(views.html.error500())
    }
  }
}

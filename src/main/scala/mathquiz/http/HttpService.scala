package mathquiz.http

import akka.actor.{ Actor, ActorSystem, Props, ActorRef, ActorLogging }
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.routing._
import spray.http._
import MediaTypes._

class MathQuizHttpServiceActor(mathQuizHttpService: MathQuizHttpService) extends HttpServiceActor {
  def receive = runRoute(mathQuizHttpService.route)
}

class MathQuizHttpService(implicit system: ActorSystem) extends HttpService {

  val actorRefFactory = system

  import Directives._

  object GetReportQueryTimeout

  val exceptionHandler = {
    ExceptionHandler {
      case e: Throwable =>
        complete(StatusCodes.InternalServerError)
    }
  }

  val rejectionHandler = {
    RejectionHandler {
      case MissingQueryParamRejection(param) :: _ =>
        complete(StatusCodes.BadRequest, s"Request is missing required query parameter '$param'")
    }
  }

  val route =
    handleRejections(rejectionHandler) {
      handleExceptions(exceptionHandler) {
        /*path("status") {
          get {
            streamStatusResponse(monitoring)
          }
        } ~*/
        pathPrefix("") {
          getFromResourceDirectory("")
        } ~
          path("") {
            getFromResource("index.html")
          }
      }
    }
}

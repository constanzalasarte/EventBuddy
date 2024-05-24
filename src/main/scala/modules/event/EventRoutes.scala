package modules.event

import modules.element.service.CheckElements
import modules.guest.CheckGuests
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{as, complete, concat, delete, entity, get, onComplete, onSuccess, parameters, path, pathEnd, post, put}
import org.apache.pekko.http.scaladsl.server.Route
import util.exceptions.{IDNotFoundException, UnacceptableException}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class EventRoutes(events: Events, guests: CheckGuests, elements: CheckElements) extends EventJsonProtocol {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  def eventRoute: Route =
    concat(
      pathEnd {
        concat(
          get {
            getEvents
          },
          post {
            entity(as[EventRequest]) { eventRequest =>
              createEvent(eventRequest)
            }
          },
        )
      },
      path("byId") {
        getByIdRoute
      },
    )

  private def getByIdRoute = {
    concat(
      get {
        parameters("id") { id =>
          getEventById(id)
        }
      },
      get {
        complete("/event/byId")
      },
      delete {
        parameters("id") { id => {
          deleteEvent(id)
        }
        }
      },
      put {
        parameters("id") { id => {
          entity(as[EventPatchRequest]) { eventPatch =>
            updateEvent(id, eventPatch)
          }
        }
        }
      }
    )
  }

  private def getEvents: Route = {
    val eventsFuture = events.getEvents
    onComplete(eventsFuture) {
      case Success(events) => complete(StatusCodes.OK, events)
      case Failure(exc) => exc match {
        case _ => internalErrorResponse
      }
    }
  }

  private def updateEvent(id: String, eventPatch: EventPatchRequest): Route = {
    try {
      val futureEvent: Future[Event] = events.changeEvent(id.toInt, eventPatch)
      onComplete(futureEvent) {
        case Success(event) => {
          complete(StatusCodes.OK, event)
        }
        case Failure(exception) => exception match {
          case e: IDNotFoundException => {
            complete(StatusCodes.UnprocessableEntity, e.getMessage)
          }
          case msg: UnacceptableException => {
            complete(StatusCodes.NotAcceptable, msg.getMessage)
          }
          case _ => {
            complete(StatusCodes.InternalServerError, "")
          }
        }
      }
    } catch {
      case _: NumberFormatException => {
        intExpectedResponse
      }
      case msg: IDNotFoundException =>
        complete(StatusCodes.UnprocessableEntity, msg.getMessage)
      case msg: UnacceptableException => {
        complete(StatusCodes.NotAcceptable, msg.getMessage)
      }
    }
  }

  private def getEventById(id: String): Route = {
    val inCaseEventExist = (event: Option[Event]) => complete(StatusCodes.OK, event.get)

    checkIfEventExist(id, inCaseEventExist)
  }


  private def checkIfEventExist(id: String, inCaseEventExist: Option[Event] => Route): Route = {
    try {
      val futureEvent: Future[Option[Event]] = events.byId(id.toInt)
      onComplete(futureEvent) {
        case Success(event) =>
          if (event.isEmpty) IDNotFoundResponse("event", id.toInt)
          else inCaseEventExist(event)
        case Failure(_) => internalErrorResponse
      }
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def deleteEvent(id: String): Route = {
    try {
      val eventual = events.deleteById(id.toInt)
      onComplete(eventual) {
        case Success(_) =>
          val deleteGuestAndElem = deleteGuestAndElement(id)
          onComplete(deleteGuestAndElem) {
            case Success(_) => complete(StatusCodes.OK, s"Event deleted")
          }
        case Failure(exception) => exception match {
          case e: IDNotFoundException => complete(StatusCodes.UnprocessableEntity, e.getMessage)
          case _ => internalErrorResponse
        }
      }
    }
    catch {
      case _: NumberFormatException =>
        intExpectedResponse
    }
  }

  private def deleteGuestAndElement(id: String): Future[Unit] = {
    for {
      _ <- elements.deleteByEventId(id.toInt)
      _ <- guests.deleteByEventId(id.toInt)
    } yield {}
  }

  private def createEvent(eventRequest: EventRequest): Route = {
    val future = events.addEvent(eventRequest)
    onComplete(future) {
      case Success(event) => complete(StatusCodes.Created, event)
      case Failure(exception) => exception match {
        case e: IDNotFoundException => complete(StatusCodes.UnprocessableEntity, e.getMessage)
        case _ => return complete(StatusCodes.InternalServerError, "")
      }
    }
  }

  private def internalErrorResponse = {
    complete(StatusCodes.InternalServerError, "")
  }

  private def IDNotFoundResponse(name: String, id: Int) =
    complete(StatusCodes.UnprocessableEntity, s"There is no $name with id $id")

  private def intExpectedResponse =
    complete(StatusCodes.NotAcceptable, "Int expected, received a no int type id")
}

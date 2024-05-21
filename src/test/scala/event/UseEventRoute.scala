package event

import modules.event.{Event, EventRequest, Events}

import java.util.Date
import server.Server.executionContext

import scala.concurrent.Await
import scala.concurrent.duration.Duration

case class UseEventRoute(events: Events) {
  def createAEvent(
                    name: String,
                    description: String,
                    creatorId: Int,
                    date: Date,
                  ): Event = {
    val eventRequest = EventRequest(name, description, creatorId, date)
    val event = addEvent(eventRequest)
    Await.result(event, Duration.Inf)
  }

  private def addEvent(eventRequest: EventRequest) = {
    for {
      event <- events.addEvent(eventRequest)
    } yield {
      event
    }
  }
}

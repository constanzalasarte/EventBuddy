package event

import java.util.Date

case class UseEventRoute(events: Events) {
  def createAEvent(
                    name: String,
                    description: String,
                    creatorId: Int,
                    date: Date,
                  ): Event = {
    val eventRequest = EventRequest(name, description, creatorId, date)
    val event = eventRequest.getEvent
    events.addEvent(event)
    event
  }
}

package event

import modules.event.Event
import org.apache.pekko.http.scaladsl.model.DateTime
import org.scalatest.flatspec.AnyFlatSpec

import java.time.Instant
import java.util.Date

class EventTest extends AnyFlatSpec{
  "Two events" should "be created and the first one must have id 1 and the second one id 2" in {
    Event.start()
    val name = "Event"
    val description = "This is an event"
    val creatorId = 1
    val date = Date.from(Instant.now())
    println(date)
    val event1 = new Event(name, description, creatorId, date)
    val event2 = new Event(name, description, creatorId, date)
    assert(event1.getId == 1)
    assert(event2.getId == 2)
  }

  "An event" should "be created" in {
    val name = "Event"
    val description = "This is an event"
    val creatorId = 1
    val date = Date.from(Instant.now())
    val event = new Event(name, description, creatorId, date)
    assert(event.getName == name)
    assert(event.getDescription == description)
    assert(event.getCreatorId == creatorId)
    assert(event.getDate == date)
  }
}

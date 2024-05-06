package event

case class Events(private var events: Set[Event]) extends CheckEvents {
  def addEvent(event: Event): Unit =
    events = events + event

  def getEvents: Set[Event] = events

  override def byId(id: Int): Option[Event] = {
    for (event <- events) {
      if(event.getId == id) return Some(event)
    }
    None
  }

  override def deleteById(id: Int): Boolean = {
    var result: Set[Event]= Set.empty
    var foundEvent: Boolean = false
    for (event <- events) {
      if(event.getId == id) foundEvent = true
      else result = result + event
    }
    if(foundEvent) events = result
    foundEvent
  }

  override def byCreatorId(id: Int): Set[Event] = {
    var result: Set[Event]= Set.empty
    for (event <- events) {
      if(event.getCreatorId == id) result = result + event
    }
    result
  }

  override def deleteByCreatorId(id: Int): Set[Event] = {
    val deleteEvents = byCreatorId(id)
    for (event <- deleteEvents) {
      deleteById(event.getId)
    }
    deleteEvents
  }

  def changeEvent(id: Int, newEvent: Event): Unit = {
    var result: Set[Event]= Set.empty
    for (event <- events) {
      if(event.getId == id) result = result + newEvent
      else result = result + event
    }
    events = result
  }
}
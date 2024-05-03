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
}
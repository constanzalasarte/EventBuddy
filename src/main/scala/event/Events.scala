package event

case class Events(private var events: Set[Event]){
  def addEvent(event: Event): Unit =
    events = events + event

  def getEvents: Set[Event] = events
}
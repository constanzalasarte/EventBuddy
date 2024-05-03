package event

import java.util.Date

case class Event(
             private val name: String,
             private val description: String,
             private val creatorId: Int,
             private val date: Date,
             private val id : Int = Event.getNewID)
{
  def getName: String = name

  def getDescription: String = description

  def getCreatorId: Int = creatorId

  def getDate: Date = date

  def getId: Int = id

  override def toString: String = s"Event(\nid: $id,\nname: $name,\ndescription: $description,\ncreatorId: $creatorId,\ndate: $getDate)"
}

private object Event {
  private var id = 0

  private def getNewID: Int = {
    incrementID()
    id
  }

  private def incrementID(): Unit = id = id+1
}

case class EventRequest(
   private val name: String,
   private val description: String,
   private val creatorId: Int,
   private val date: Date)
{
  def getName: String = name

  def getDescription: String = description

  def getCreatorId: Int = creatorId

  def getDate: Date = date

  def getEvent: Event = Event(name, description, creatorId, date)
}

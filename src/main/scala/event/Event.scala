package event

import java.util.Date

case class Event(
             private var name: String,
             private var description: String,
             private var creatorId: Int,
             private var date: Date,
             private val id : Int = Event.getNewID)
{
  def getName: String = name

  def getDescription: String = description

  def getCreatorId: Int = creatorId

  def getDate: Date = date

  def getId: Int = id

  def changeName(newName: String): Unit = name = newName

  def changeDescription(newDescription: String): Unit = description = newDescription

  def changeCreatorId(newCreatorId:Int): Unit = creatorId = newCreatorId

  def changeDate(newDate: Date): Unit = date = newDate

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

case class EventPatchRequest(
   name: Option[String],
   description: Option[String],
   creatorId: Option[Int],
   date: Option[Date])
{
  def hasName: Boolean = name.isDefined

  def hasDescription: Boolean = description.isDefined

  def hasCreatorId: Boolean = creatorId.isDefined

  def hasDate: Boolean = date.isDefined

}

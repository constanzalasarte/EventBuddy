package element

import java.util.Date

case class Element(
             private var name: String,
             private var quantity: Int,
             private var eventId: Int,
             private var maxUsers: Int,
             private var users: Set[Int],
             private val id : Int = Element.getNewID)
{
  def getName: String = name

  def getQuantity: Int = quantity

  def getEventId: Int = eventId

  def getMaxUsers: Int = maxUsers

  def getId: Int = id

  def getUsers: Set[Int] = users

  def changeName(newName: String): Unit = name = newName

  def changeQuantity(newQty: Int): Unit = quantity = newQty

  def changeEventId(newEventId:Int): Unit = eventId = newEventId

  def changeMaxUsers(newMaxUsers: Int): Unit = maxUsers = newMaxUsers

  def changeUsers(newUsers: Set[Int]): Unit = users = newUsers
}

private object Element {
  private var id = 0

  private def getNewID: Int = {
    incrementID()
    id
  }

  private def incrementID(): Unit = id = id+1
}

case class ElementRequest(
                         name: String,
                         quantity: Int,
                         eventId: Int,
                         maxUsers: Int,
                         users: Set[Int])
{
  def getElement: Element = Element(name, quantity, eventId, maxUsers, users)
}

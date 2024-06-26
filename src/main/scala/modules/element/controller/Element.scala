package modules.element.controller

case class Element(
             var name: String,
             var quantity: Int,
             var eventId: Int,
             var maxUsers: Int,
             var users: Set[Int],
             id : Int = Element.getNewID)
{
  def getName: String = name

  def getMaxUsers: Int = maxUsers

  def getId: Int = id

  def getEventId: Int = eventId

  def getUsers: Set[Int] = users

  def getQty: Int = quantity

  def changeName(newName: String): Unit = name = newName

  def changeQuantity(newQty: Int): Unit = quantity = newQty

  def changeEventId(newEventId:Int): Unit = eventId = newEventId

  def changeMaxUsers(newMaxUsers: Int): Unit = maxUsers = newMaxUsers

  def changeUsers(newUsers: Set[Int]): Unit = users = newUsers

  def deleteUserInUsers(id: Int): Unit = {
    var result: Set[Int] = Set.empty
    for(user <- users){
      if(user != id) result = result + user
    }
    users = result
  }

  def isUserInUsers(id: Int): Boolean = {
    for(user <- getUsers){
      if(user == id) return true
    }
    false
  }
}

object Element {
  private var id = 0

  def start(): Unit = id = 0

  private def getNewID: Int = {
    incrementID()
    id
  }


  private def incrementID(): Unit = id = id+1
}

package element.controller

case class Element(
             private var name: String,
             private var quantity: Int,
             private var eventId: Int,
             private var maxUsers: Int,
             private var users: Set[Int],
             private val id : Int = Element.getNewID)
{
  def getName: String = name

  def getMaxUsers: Int = maxUsers

  def getId: Int = id

  def getEventId: Int = eventId

  def getUsers: Set[Int] = users

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

private object Element {
  private var id = 0

  private def getNewID: Int = {
    incrementID()
    id
  }

  private def incrementID(): Unit = id = id+1
}

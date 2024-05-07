package element

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

case class ElementRequest(
                         name: String,
                         quantity: Int,
                         eventId: Int,
                         maxUsers: Int,
                         users: Set[Int])
{
  def getElement: Element = Element(name, quantity, eventId, maxUsers, users)
}

case class ElementPatchRequest(
  name: Option[String] = None,
  quantity: Option[Int] = None,
  eventId: Option[Int] = None,
  maxUsers: Option[Int] = None,
  users: Option[Set[Int]] = None
){
  def hasName: Boolean = name.isDefined
  def hasQty: Boolean = quantity.isDefined
  def hasEventId: Boolean = eventId.isDefined
  def hasMaxUsers: Boolean = maxUsers.isDefined
  def hasUsers: Boolean = users.isDefined
}

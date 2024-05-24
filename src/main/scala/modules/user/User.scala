package modules.user

case class User(var email: String, var userName: String, id: Int = User.getNewID) {
  def getEmail: String = email

  def getUserName: String = userName

  def changeUserName(newUserName: String) = userName = newUserName
  def changeEmail(newEmail: String) = email = newEmail

  def getId: Int = id
}

object User {
  private var id = 0

  def start(): Unit = id = 0
  private def getNewID: Int = {
    incrementID()
    id
  }

  private def incrementID(): Unit = id = id+1
}

case class UserRequest(email: String, userName: String) {
  def getUser: User = User(email, userName)
}

case class UserPatchRequest(email: Option[String], userName: Option[String]) {
  def hasEmail: Boolean = email.isDefined
  def hasUserName: Boolean = userName.isDefined
}

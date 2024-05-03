package user


case class User(private val email: String, private val userName: String, private val id: Int = User.getNewID) {
  def getEmail: String = email

  def getUserName: String = userName

  def getId: Int = id

  override def toString: String = s"User(id: $id, email: $email, userName: $userName)"
}

object User {
  private var id = 0

  private def getNewID: Int = {
    incrementID()
    id
  }

  private def incrementID(): Unit = id = id+1
}

case class UserRequest(email: String, userName: String) {
  def getEmail: String = email
  def getUserName: String = userName
  def getUser: User = User(email, userName)
}

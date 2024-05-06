package user

case class Users(private var users: Set[User]) extends CheckUsers {
  def addUser(user: User) : Unit =
    users = users + user

  def getUsers: Set[User] = users

  def changeUser(id: Int, newUser: User): Unit = {
    var result: Set[User]= Set.empty
    for (user <- users) {
      if(user.getId == id) result = result + newUser
      else result = result + user
    }
    users = result
  }

  override def byID(id: Int): Option[User] = {
    for (user <- users) {
      if(user.getId == id) return Some(user)
    }
    None
  }

  override def deleteById(id: Int): Boolean = {
    var result: Set[User]= Set.empty
    var foundUser: Boolean = false
    for (user <- users) {
      if(user.getId == id) foundUser = true
      else result = result + user
    }
    if(foundUser) users = result
    foundUser
  }
}

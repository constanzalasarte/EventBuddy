package modules.user.repository

import modules.user.User

case class SetUserRepo(private var users: Set[User]) extends UserRepository {

  override def addUser(user: User): Unit =
    users = users + user

  override def getUsers: Set[User] = users

  override def updateUser(id: Int, newUser: User): Unit = {
    var result: Set[User]= Set.empty
    for (user <- users) {
      if(user.getId == id) result = result + newUser
      else result = result + user
    }
    users = result
  }

  override def byID(id: Int): Option[User] =
    users.find(user => user.getId == id)

  override def deleteById(id: Int): Boolean ={
    val maybeUser = users.find(_.getId == id)
    maybeUser.foreach { found =>
      users = users - found
    }
    maybeUser.isDefined
  }
}

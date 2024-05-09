package user

import user.repository.{SetUserRepo, UserRepository}
import util.Version

object UserServiceFactory{
  def createService(version: Version): Users =
    version match {
      case Version.SetVersion => Users(SetUserRepo(Set.empty))
    }
}

case class Users(private var repository: UserRepository) extends CheckUsers {
  def addUser(user: User) : Unit =
    repository.addUser(user)

  def getUsers: Set[User] = repository.getUsers

  def changeUser(id: Int, newUser: User): Unit = {
    repository.updateUser(id, newUser)
  }

  override def byID(id: Int): Option[User] = {
    repository.byID(id)
  }

  override def deleteById(id: Int): Boolean = {
    repository.deleteById(id)
  }
}

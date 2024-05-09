package user.repository

import user.User

trait UserRepository {
  def addUser(user: User): Unit
  def getUsers: Set[User]
  def updateUser(id: Int, newUser: User): Unit
  def byID(id: Int): Option[User]
  def deleteById(id: Int): Boolean
}

package modules.user.repository

import modules.user.User

import scala.concurrent.Future

trait UserRepository {
  def addUser(user: User): Future[Unit]
  def getUsers(): Future[Set[User]]
  def updateUser(id: Int, newUser: User): Future[Unit]
  def byID(id: Int): Future[Option[User]]
  def deleteById(id: Int): Future[Unit]

  /**
   * Checks if there is any id that no belong
   * to a user in the set of ids
   * @param ids
   * @return the no user related ids
   */
  def noUserIds(ids: Set[Int]): Future[Option[Set[Int]]]
}

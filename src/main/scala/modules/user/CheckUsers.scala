package modules.user

import scala.concurrent.Future

trait CheckUsers {
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

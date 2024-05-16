package modules.user

import scala.concurrent.Future

trait CheckUsers {
  def byID(id: Int): Future[Option[User]]
  def deleteById(id: Int): Future[Unit]
}

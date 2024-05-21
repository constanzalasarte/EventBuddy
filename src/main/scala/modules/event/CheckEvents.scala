package modules.event

import scala.concurrent.Future

trait CheckEvents {
  def byId(id: Int): Future[Option[Event]]
  def byCreatorId(id: Int): Future[Set[Event]]
  def deleteById(id: Int): Future[Unit]
  def deleteByCreatorId(id: Int): Future[Set[Event]]
}

package modules.guest.repository
import modules.guest.Guest
import slick.jdbc.JdbcBackend.Database
import server.Server.executionContext
import slick.jdbc.PostgresProfile.api._
import util.DBTables
import util.DBTables.{GuestEntity, eventTable}

import scala.concurrent.Future

case class GuestDBRepo(db: Database) extends GuestRepository {
  private val guestTable = DBTables.guestTable

  override def addGuest(guest: Guest): Future[Unit] = {
    val q = DBIO.seq(guestTable += transformGuest(guest))
    for {
      _ <- db.run(q)
    } yield {}
  }


  override def getGuests: Future[Set[Guest]] = {
    val q = guestTable.sortBy(_.id).result
    for{
      seqGuest <- db.run(q)
    } yield {
      transformToGuestSet(seqGuest)
    }
  }

  override def changeGuestById(id: Int, newGuest: Guest): Future[Unit] = {
    val guestEntity = transformGuest(newGuest)
    val q = guestTable.filter(_.id === id).update(guestEntity)
    for {
      _ <- db.run(q)
    } yield {}
  }

  override def byId(id: Int): Future[Option[Guest]] = {
    val q = guestTable.filter(_.id === id)
    for{
      guestEntity <- db.run(q.result.headOption)
    } yield {
      if(guestEntity.isEmpty) None
      else Some(transformGuestEntity(guestEntity.get))
    }
  }

  override def deleteById(id: Int): Future[Boolean] = {
    val q = eventTable.filter(_.id === id).delete
    for {
      deleted <- db.run(q)
    } yield {
      deleted == 1
    }
  }

  private def transformGuest(guest: Guest): GuestEntity =
    GuestEntity(Some(guest.getId), guest.getUserId, guest.getEventId, guest.getConfirmationStatus, guest.getIsHost)

  private def transformToGuestSet(seqGuest: Seq[GuestEntity]): Set[Guest] =
    seqGuest.toSet.map(
      guestEntity => transformGuestEntity(guestEntity)
    )

  private def transformGuestEntity(guestEntity: GuestEntity) =
    Guest(guestEntity.userId, guestEntity.eventId, guestEntity.confirmationStatus, guestEntity.isHost, guestEntity.id.get)
}

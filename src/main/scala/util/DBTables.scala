package util

import modules.guest.ConfirmationStatus
import modules.guest.ConfirmationStatus.ConfirmationStatus
import slick.ast.BaseTypedType
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDate
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.{Duration, DurationInt}
import server.Server.executionContext

object DBTables {
  case class UserEntity(id: Option[Int], email: String, userName: String)

  class UserTable(tag: Tag) extends Table[UserEntity](tag, "users") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL")
    def userName = column[String]("USERNAME")
    override def * =
      (id.?, email, userName) <> (UserEntity.tupled, UserEntity.unapply)
  }

  val userTable = TableQuery[UserTable]

  case class EventEntity(id: Option[Int], name: String, description: String, creatorId: Int, date: LocalDate)

  class EventTable(tag: Tag) extends Table[EventEntity](tag, "events") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def description = column[String]("DESCRIPTION")
    def creatorId = column[Int]("CREATOR_ID")
    def date = column[LocalDate]("DATE")
    def creator = foreignKey("CREATOR", creatorId, userTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    override def * =
      (id.?, name, description, creatorId, date) <> (EventEntity.tupled, EventEntity.unapply)
  }

  val eventTable = TableQuery[EventTable]

  case class GuestEntity(id: Option[Int], userId: Int, eventId: Int, confirmationStatus: ConfirmationStatus, isHost: Boolean)

  class GuestTable(tag: Tag) extends Table[GuestEntity](tag, "guests") {
    implicit val confirmationMapper: JdbcType[ConfirmationStatus] with BaseTypedType[ConfirmationStatus] =
      MappedColumnType.base[ConfirmationStatus, String](
        e => e.toString,
        s => ConfirmationStatus.withName(s)
      )

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userID = column[Int]("USER_ID")
    def eventId = column[Int]("EVENT_ID")
    def confirmationStatus = column[ConfirmationStatus]("CONFIRMATION_STATUS")
    def isHost = column[Boolean]("IS_HOST")
    override def * =
      (id.?, userID, eventId, confirmationStatus, isHost) <> (GuestEntity.tupled, GuestEntity.unapply)

    def user =
      foreignKey("USER_FK", userID, userTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def event =
      foreignKey("EVENT_FK", eventId, eventTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  }

  val guestTable = TableQuery[GuestTable]

  case class ElementEntity(id: Option[Int], name: String, quantity: Int, eventId: Int, maxUsers: Int)

  class ElementTable(tag: Tag) extends Table[ElementEntity](tag, "elements") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name = column[String]("NAME")
    def quantity = column[Int]("QUANTITY")
    def eventId = column[Int]("EVENT_ID")
    def maxUsers = column[Int]("MAX_USERS")
    def event = foreignKey("EVENT", eventId, eventTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    override def * =
      (id.?, name, quantity, eventId, maxUsers) <> (ElementEntity.tupled, ElementEntity.unapply)
  }

  val elementTable = TableQuery[ElementTable]

  case class UserElementEntity(id: Option[Int], userId: Int, elementId: Int)

  class UserElementTable(tag: Tag) extends Table[UserElementEntity](tag, "userElement") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("USER_ID")
    def elementId = column[Int]("ELEMENT_ID")
    def user = foreignKey("USER", userId, userTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def element = foreignKey("ELEMENT", elementId, elementTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    override def * =
      (id.?, userId, elementId) <> (UserElementEntity.tupled, UserElementEntity.unapply)
  }

  val userElement = TableQuery[UserElementTable]

  def createSchema(db: Database): Future[Database] = {
    for {
      _ <-db.run(userTable.schema.createIfNotExists)
      _ <- db.run(eventTable.schema.createIfNotExists)
      _ <- db.run(guestTable.schema.createIfNotExists)
      _ <- db.run(elementTable.schema.createIfNotExists)
      _ <-db.run(userElement.schema.createIfNotExists)
    } yield{
      db
    }
  }
  def dropSchema(db: Database): Unit = {
    Await.result(db.run(DBIO.seq(
      userElement.schema.drop,
      elementTable.schema.drop,
      guestTable.schema.drop,
      eventTable.schema.drop,
      userTable.schema.drop)
    ), 2.seconds)
  }
  def close(db: Database): Unit = {
    db.close()
  }
}

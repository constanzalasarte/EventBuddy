package util

import modules.guest.ConfirmationStatus
import modules.guest.ConfirmationStatus.ConfirmationStatus
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.PostgresProfile.api._

object DBTables {
  case class UserEntity(id: Option[Int], email: String, userName: String)

  class UserTable(tag: Tag) extends Table[UserEntity](tag, "users") {
    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def email: slick.lifted.Rep[String] = column[String]("EMAIL")
    def userName = column[String]("USERNAME")
    override def * =
      (id.?, email, userName) <> (UserEntity.tupled, UserEntity.unapply)
  }

  val userTable = TableQuery[UserTable]

//  case class EventEntity(id: Option[Long], email: String, userName: String)
//
//  class EventTable(tag: Tag) extends Table[UserEntity](tag, "users") {
//    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
//    def email: slick.lifted.Rep[String] = column[String]("EMAIL")
//    def userName = column[String]("USERNAME")
//    override def * =
//      (id.?, email, userName) <> (UserEntity.tupled, UserEntity.unapply)
//  }
//
//  val eventTable = TableQuery[UserTable]

  case class GuestEntity(id: Option[Long], userId: Int, eventId: Int, confirmationStatus: ConfirmationStatus, isHost: Boolean)

  class GuestTable(tag: Tag) extends Table[GuestEntity](tag, "guests") {
    implicit val confirmationMapper: JdbcType[ConfirmationStatus] with BaseTypedType[ConfirmationStatus] =
      MappedColumnType.base[ConfirmationStatus, String](
        e => e.toString,
        s => ConfirmationStatus.withName(s)
      )

    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def userID = column[Int]("USER_ID")
    def user = foreignKey("USER_FK", userID, userTable)(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
    def eventId = column[Int]("EVENT_ID")
    def confirmationStatus = column[ConfirmationStatus]("CONFIRMATION_STATUS")
    def isHost = column[Boolean]("IS_HOST")
    override def * =
      (id.?, userID, eventId, confirmationStatus, isHost) <> (GuestEntity.tupled, GuestEntity.unapply)
  }

  val guestTable = TableQuery[GuestTable]
}

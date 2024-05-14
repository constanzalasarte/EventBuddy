package modules.user.repository
import slick.jdbc.JdbcBackend.Database
import modules.user.User
import server.Server.executionContext
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future

case class UserEntity(id: Option[Long], email: String, userName: String)

class UserTable(tag: Tag) extends Table[UserEntity](tag, "users") {
  def id = column[Long]("USER_ID", O.PrimaryKey, O.AutoInc)
  def email: slick.lifted.Rep[String] = column[String]("EMAIL")
  def userName = column[String]("USERNAME")
  override def * =
    (id.?, email, userName) <> (UserEntity.tupled, UserEntity.unapply)
}

case class UserSlickRepo(userTable: TableQuery[UserTable], db: Database) extends UserRepository{
  override def addUser(user: User): Future[Unit] = {
    val action = DBIO.seq(userTable += transformUser(user))
    for {
      _ <- db.run(action)
    } yield {}
  }

  def getUsers(): Future[Set[User]] = {
    for {
      result <- db.run(userTable.sortBy(_.id).result)
    } yield {
      transformToUserSet(result)
    }
  }


  override def updateUser(id: Int, newUser: User): Future[Unit] = ???

  override def byID(id: Int): Future[Option[User]] = ???

  override def deleteById(id: Int): Future[Boolean] = ???
//  {
//    val q = userTable.filter(_.id === id)
//    val action = q.delete
//    val affectedRowsCount = db.run(action)
//    affectedRowsCount.onComplete{
//      case util.Success(value) => value != 0
//    }
//    false
//  }

  private def transformUser(user: User): UserEntity =
    UserEntity(Some(user.getId), user.getEmail, user.getUserName)

  private def transformUserEntity(userEntity: UserEntity) =
    User(userEntity.email, userEntity.userName, userEntity.id.get.toInt)

  private def transformToUserSet(seq: Seq[UserEntity]): Set[User] =
    seq.toSet.map(
      userEntity => transformUserEntity(userEntity)
    )

}


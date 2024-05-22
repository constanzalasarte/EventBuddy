package modules.user.repository
import slick.jdbc.JdbcBackend.Database
import modules.user.User
import server.Server.executionContext
import slick.jdbc.PostgresProfile.api._
import util.DBTables
import util.DBTables.UserEntity

import scala.concurrent.Future

case class UserDBRepo(db: Database) extends UserRepository{
  private val userTable = DBTables.userTable
  override def addUser(user: User): Future[Unit] = {
    val action = DBIO.seq(userTable += transformUser(user))
    for {
      _ <- db.run(action)
    } yield {}
  }

  def getUsers(): Future[Set[User]] =
    for {
      result <- db.run(userTable.sortBy(_.id).result)
    } yield {
      transformToUserSet(result)
    }


  override def updateUser(id: Int, newUser: User): Future[Unit] = {
    val userEntity = transformUser(newUser)
    val q = userTable.filter(_.id === id).update(userEntity)
    for{
      _ <- db.run(q)
    } yield {}
  }

  override def byID(id: Int): Future[Option[User]] = {
    val q = userTable.filter(_.id === id)
    for{
      r <- db.run(q.result.headOption)
    } yield {
      transformToUser(r)
    }
  }

  override def deleteById(id: Int): Future[Unit] = {
    val q = userTable.filter(_.id === id).delete
    for{
      deleted <- db.run(q)
    } yield {
      deleted == 1
    }
  }

  private def transformUser(user: User): UserEntity =
    UserEntity(Some(user.getId), user.getEmail, user.getUserName)

  private def transformUserEntity(userEntity: UserEntity) =
    User(userEntity.email, userEntity.userName, userEntity.id.get)

  private def transformToUserSet(seq: Seq[UserEntity]): Set[User] =
    seq.toSet.map(
      userEntity => transformUserEntity(userEntity)
    )

  private def transformToUser(userEntity: Option[UserEntity]): Option[User] = {
    if(userEntity.isEmpty) None
    else Some(transformUserEntity(userEntity.get))
  }
}


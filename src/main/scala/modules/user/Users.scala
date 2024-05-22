package modules.user

import modules.user.repository.{UserRepository, UserDBRepo}
import slick.jdbc.JdbcBackend.Database
import util.Version
import util.Version.{DBVersion, SetVersion}

import scala.concurrent.Future
import server.Server.executionContext

object UserServiceFactory{
  def createService(version: Version, db: Option[Database] = None): Users =
    version match {
      case DBVersion => Users(UserDBRepo(db.get))
    }
}

case class Users(private var repository: UserRepository) extends CheckUsers {
  def addUser(user: User) : Future[Unit] =
    repository.addUser(user)

  def getUsers(): Future[Set[User]] =
    repository.getUsers()

  def changeUser(id: Int, newUser: User): Future[Unit] = {
    repository.updateUser(id, newUser)
  }

  override def byID(id: Int): Future[Option[User]] =
    repository.byID(id)

  override def deleteById(id: Int): Future[Unit] = {
    repository.deleteById(id)
  }

  override def noUserIds(ids: Set[Int]): Future[Set[Int]] = {
    for{
      userIds <- repository.getUsers()
    } yield {
      ids.filterNot(id => idIsInsideUserId(id, userIds))
    }
  }
  private def idIsInsideUserId(id: Int, users: Set[User]): Boolean =
    users.exists(user => user.getId == id)
}

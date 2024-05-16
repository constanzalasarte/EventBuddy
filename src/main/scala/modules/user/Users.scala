package modules.user

import modules.user.repository.{SetUserRepo, UserRepository, UserSlickRepo}
import slick.jdbc.JdbcBackend.Database
import util.Version
import util.Version.{DBVersion, SetVersion}

import scala.concurrent.Future

object UserServiceFactory{
  def createService(version: Version, db: Option[Database] = None): Users =
    version match {
      case SetVersion => Users(SetUserRepo(Set.empty))
      case DBVersion => Users(UserSlickRepo(db.get))
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
}

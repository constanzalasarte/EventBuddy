package modules.user.repository

import modules.user.User
import server.Server.executionContext

import scala.concurrent.Future

case class SetUserRepo(private var users: Set[User]) extends UserRepository {

  override def addUser(user: User): Future[Unit] = {
    users = users + user
    Future{}
  }

  override def getUsers(): Future[Set[User]] = Future{users}

  override def updateUser(id: Int, newUser: User): Future[Unit] = {
    var result: Set[User]= Set.empty
    for (user <- users) {
      if(user.getId == id) result = result + newUser
      else result = result + user
    }
    users = result
    Future{}
  }

  override def byID(id: Int): Future[Option[User]] = Future {
    users.find(user => user.getId == id)
  }

  override def deleteById(id: Int): Future[Unit] ={
    val maybeUser = users.find(_.getId == id)
    maybeUser.foreach { found =>
      users = users - found
    }
    Future { }
  }
}

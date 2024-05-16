package user

import modules.user.User
import modules.user.repository.UserSlickRepo
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.PostgresProfile.api._
import util.DBTables.userTable

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class UserSlickRepoTest extends AsyncWordSpec with Matchers with BeforeAndAfterEach{
  var db: Database = Database.forConfig("eventBuddy-db")

  override protected def beforeEach(): Unit = {
    db = Database.forConfig("eventBuddy-db")
    Await.result(db.run(userTable.schema.create), 2.seconds)
  }

  override protected def afterEach(): Unit = {
    Await.result(db.run(userTable.schema.drop), 2.seconds)
    db.close
  }

  "get no users with slick" in {
    val slickRepo = UserSlickRepo(db)
    val set = slickRepo.getUsers()
    set.map { x: Set[User] =>
      x should have size 0
    }
  }

  "get no users" in {
    val q1 = userTable.sortBy(_.email)
    for {
      results <- db.run(q1.result)
    } yield {
      results should have size 0
    }
  }

  "add user with slick" in {
    val slickRepo = UserSlickRepo(db)
    slickRepo.addUser(User("email@test.com", "userName", 10))
    slickRepo.addUser(User("email2@test.com", "userName", 10))
    slickRepo.addUser(User("email3@test.com", "userName", 10))
    val set = slickRepo.getUsers()
    set.map { x: Set[User] =>
      x should have size 3
    }
  }
}

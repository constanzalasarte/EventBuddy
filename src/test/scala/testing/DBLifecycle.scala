package testing

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite}
import util.DBTables.{createSchema, dropSchema}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

trait DBLifecycle extends Suite with BeforeAndAfterEach with BeforeAndAfterAll { this: H2Capabilities =>

  override protected def beforeAll(): Unit = {
    val futureDB = createSchema(db)
    db = Await.result(futureDB, Duration.Inf)
  }

  override protected def beforeEach(): Unit = {
//    deleteRows(db)
  }

  override protected def afterEach(): Unit = {
    dropSchema(db)
    val futureDB = createSchema(db)
    db = Await.result(futureDB, Duration.Inf)
  }

  override protected def afterAll(): Unit = {
    dropSchema(db)
  }
}

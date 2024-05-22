package testing

import slick.jdbc.JdbcBackend.Database

trait H2Capabilities {

  var db = Database.forConfig("eventBuddy-db")
}

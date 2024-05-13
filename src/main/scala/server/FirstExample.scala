package server

import slick.jdbc.JdbcBackend.Database

object FirstExample {
  def main(args: Array[String]): Unit = {
    val db: Database = Database.forConfig("my-db")
    db.close
  }

}

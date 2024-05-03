package routes

import event.Events
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import user.Users

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object PrincipalRoute extends ServerRoutes {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  private val config = system.settings.config
  private val interface = config.getString("app.interface")
  private val port = config.getInt("app.port")

  def startRoutes(): Unit = {
    val users = Users(Set.empty)
    val events = Events(Set.empty)
    val bindingFuture = Http().newServerAt(interface, port).bind(combinedRoutes(users, events))
    println(s"Server online\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

  def main(args: Array[String]): Unit = {
    startRoutes()
  }

}

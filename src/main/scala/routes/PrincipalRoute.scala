package routes

import element.{CreateElements, Elements}
import element.Version.SetVersion
import event.Events
import guest.Guests
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.server.Route
import user.Users

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object PrincipalRoute extends ServerRoutes {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "SprayExample")
  implicit val executionContext: ExecutionContext = system.executionContext

  private val config = system.settings.config
  private val interface = config.getString("app.interface")
  private val port = config.getInt("app.port")

  def setUpElements(): Elements ={
    val createElem = CreateElements
    createElem.createElements(SetVersion)
  }

  def startRoutes(): Unit = {
    val users = Users(Set.empty)
    val events = Events(Set.empty)
    val guests = Guests(Set.empty)
    val elements = setUpElements()
    val bindingFuture = Http().newServerAt(interface, port).bind(combinedRoutes(users, events, guests, elements))
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

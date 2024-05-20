import Receptionist.Job
import org.apache.pekko.actor.{Actor, ActorRef}

class Receptionist extends Actor {

  override def receive: Receive = {
    case Job =>
  }

}

object Receptionist {
  case class Job(client: ActorRef, url: String)
  case class Result(url: String, urls: Set[String])
  case class Failed(url: String, reason: String)
}
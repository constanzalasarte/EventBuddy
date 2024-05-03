package routes

import event.{EventRoutes, Events}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import org.apache.pekko.http.scaladsl.server.Directives.pathPrefix
import user.{UserRoutes, Users}

trait ServerRoutes {
  def combinedRoutes(users: Users, events: Events): Route = {
    val userRoutes = UserRoutes(users)
    val eventRoute = EventRoutes(events, users)

    Directives.concat(
      pathPrefix("event") {
        eventRoute.eventRoute
      },
      pathPrefix("user") {
        userRoutes.userRoute
      }
    )
  }
}

package routes

import event.{EventRoutes, Events}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import org.apache.pekko.http.scaladsl.server.Directives.path
import user.{UserRoutes, Users}

trait ServerRoutes {
  def combinedRoutes(users: Users, events: Events): Route = {
    val userRoutes = UserRoutes(users)
    val eventRoute = EventRoutes(events, users)

    Directives.concat(
      path("event") {
        eventRoute.eventRoute
      },
      path("user") {
        userRoutes.userRoute
      }
    )
  }
}

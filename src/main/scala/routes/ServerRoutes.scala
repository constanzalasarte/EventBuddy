package routes

import event.{EventRoutes, Events}
import guest.{GuestRoutes, Guests}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import org.apache.pekko.http.scaladsl.server.Directives.pathPrefix
import user.{UserRoutes, Users}

trait ServerRoutes {
  def combinedRoutes(users: Users, events: Events, guests: Guests): Route = {
    val userRoutes = UserRoutes(users, events)
    val eventRoute = EventRoutes(events, users)
    val guestRoute = GuestRoutes(guests, events, users)

    Directives.concat(
      pathPrefix("event") {
        eventRoute.eventRoute
      },
      pathPrefix("user") {
        userRoutes.userRoute
      },
      pathPrefix("guest") {
        guestRoute.guestRoute
      }
    )
  }
}

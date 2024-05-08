package routes

import element.{ElementRoutes, ElementService}
import event.{EventRoutes, Events}
import guest.{GuestRoutes, Guests}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import org.apache.pekko.http.scaladsl.server.Directives.pathPrefix
import user.{UserRoutes, Users}

trait ServerRoutes {
  def combinedRoutes(users: Users, events: Events, guests: Guests, elements: ElementService): Route = {
    val userRoutes = UserRoutes(users, events, guests, elements)
    val eventRoute = EventRoutes(events, users, guests, elements)
    val guestRoute = GuestRoutes(guests, events, users)
    val elementRoute = ElementRoutes(elements, events, users)

    Directives.concat(
      pathPrefix("event") {
        eventRoute.eventRoute
      },
      pathPrefix("user") {
        userRoutes.userRoute
      },
      pathPrefix("guest") {
        guestRoute.guestRoute
      },
      pathPrefix("element") {
        elementRoute.elementRoute
      }
    )
  }
}

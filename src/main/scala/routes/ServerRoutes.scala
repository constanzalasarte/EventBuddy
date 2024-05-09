package routes

import element.service.ElementService
import element.ElementRoutes
import event.{EventRoutes, Events}
import guest.{GuestRoutes, Guests}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import org.apache.pekko.http.scaladsl.server.Directives.pathPrefix
import user.{UserRoutes, Users}

trait ServerRoutes {
  def combinedRoutes(users: Users, events: Events, guests: Guests, elementService: ElementService): Route = {
    val userRoutes = UserRoutes(users, events, guests, elementService)
    val eventRoute = EventRoutes(events, users, guests, elementService)
    val guestRoute = GuestRoutes(guests, events, users)
    val elementRoute = ElementRoutes(elementService)

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

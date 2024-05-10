package routes

import modules.element.controller.{ElementRouteFactory, ElementRoutes}
import modules.element.service.ElementService
import modules.event.{EventRoutes, Events}
import modules.guest.{GuestRoutes, Guests}
import modules.user.{UserRoutes, Users}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import org.apache.pekko.http.scaladsl.server.Directives.pathPrefix

trait ServerRoutes {
  def combinedRoutes(users: Users, events: Events, guests: Guests, elementService: ElementService): Route = {
    val userRoutes = UserRoutes(users, events, guests, elementService)
    val eventRoute = EventRoutes(events, users, guests, elementService)
    val guestRoute = GuestRoutes(guests, events, users)
    val elementRoute = setUpElementRoutes(elementService)

    Directives.concat(
      pathPrefix("modules/event") {
        eventRoute.eventRoute
      },
      pathPrefix("modules/user") {
        userRoutes.userRoute
      },
      pathPrefix("modules/guest") {
        guestRoute.guestRoute
      },
      pathPrefix("modules/element") {
        elementRoute.elementRoute
      }
    )
  }

  private def setUpElementRoutes(elementService: ElementService): ElementRoutes = {
    val factory = ElementRouteFactory
    factory.create(elementService)
  }
}

package guest

import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.Directives.{complete, concat, get, pathEnd}

case class GuestRoutes(guests: Guests) extends GuestJsonProtocol {
  def guestRoute: Route =
    concat(
      pathEnd{
        concat(
          get{
            complete(StatusCodes.OK, guests.getGuests)
          }
        )
      }
    )
}

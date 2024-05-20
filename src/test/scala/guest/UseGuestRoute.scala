package guest

import modules.guest.ConfirmationStatus.ConfirmationStatus
import modules.guest.{Guest, GuestRequest, Guests}
import org.apache.pekko.actor.InvalidMessageException
import util.{Created, Error, Ok, Result}
import server.Server.executionContext

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration


case class UseGuestRoute(guests: Guests) {
  def createAGuest(
                    userId: Int,
                    eventId: Int,
                    confirmationStatus: ConfirmationStatus,
                    isHost: Boolean,
                  ): Guest = {
    val guestRequest = GuestRequest(userId, eventId, confirmationStatus, isHost)
    waitForAdd(guestRequest)
  }

  private def waitForAdd(guestRequest: GuestRequest): Guest = {
    val f = addGuest(guestRequest)
    Await.result(f, Duration.Inf)
  }
  private def addGuest(guestRequest: GuestRequest): Future[Guest] = {
    for {
      guest <- guests.addGuest(guestRequest)
    } yield {
      guest
    }
  }
}

package guest

import modules.guest.ConfirmationStatus.ConfirmationStatus
import modules.guest.{Guest, GuestRequest, Guests}
import org.apache.pekko.actor.InvalidMessageException
import util.{Created, Error, Ok}

case class UseGuestRoute(guests: Guests) {
  def createAGuest(
                    userId: Int,
                    eventId: Int,
                    confirmationStatus: ConfirmationStatus,
                    isHost: Boolean,
                  ): Guest = {
    val guestRequest = GuestRequest(userId, eventId, confirmationStatus, isHost)
    val result = guests.addGuest(guestRequest)
    result match {
      case Error(_) => throw InvalidMessageException("you are testing something that is already tested\nplease try to create a valid guest!")
      case Ok(ok) => ok
      case Created(created) => created
    }
  }
}

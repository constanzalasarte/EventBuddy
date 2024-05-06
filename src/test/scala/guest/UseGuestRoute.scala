package guest

import guest.ConfirmationStatus.ConfirmationStatus

case class UseGuestRoute(guests: Guests) {
  def createAGuest(
                    userId: Int,
                    eventId: Int,
                    confirmationStatus: ConfirmationStatus,
                    isHost: Boolean,
                  ): Guest = {
    val guestRequest = GuestRequest(userId, eventId, confirmationStatus, isHost)
    val guest = guestRequest.getGuest
    guests.addGuest(guest)
    guest
  }
}

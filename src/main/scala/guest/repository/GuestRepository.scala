package guest.repository

import guest.Guest

trait GuestRepository {
  def addGuest(guest: Guest): Unit

  def getGuests: Set[Guest]

  def changeGuestById(id: Int, newGuest: Guest): Unit

  def byId(id: Int): Option[Guest]

  def deleteById(id: Int): Boolean
}

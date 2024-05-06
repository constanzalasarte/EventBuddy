package guest

case class Guests(private var guests: Set[Guest]){
  def addGuest(guest: Guest): Unit =
    guests = guests + guest

  def getGuests: Set[Guest] = guests
}

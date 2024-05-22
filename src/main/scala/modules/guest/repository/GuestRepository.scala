package modules.guest.repository

import modules.guest.Guest

import scala.concurrent.Future

trait GuestRepository {
  def addGuest(guest: Guest): Future[Unit]

  def getGuests: Future[Set[Guest]]

  def changeGuestById(id: Int, newGuest: Guest): Future[Unit]

  def byId(id: Int): Future[Option[Guest]]

  def deleteById(id: Int): Future[Boolean]
}

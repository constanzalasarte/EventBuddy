package guest

import guest.ConfirmationStatus.ConfirmationStatus

case class Guest(
  private val userId: Int,
  private val eventId: Int,
  private val confirmationStatus: ConfirmationStatus,
  private val isHost: Boolean,
  private val id: Int = Guest.getNewID,
){
  def getUserId: Int = userId
  def getEventId: Int = eventId
  def getConfirmationStatus: ConfirmationStatus = confirmationStatus
  def getIsHost: Boolean = isHost
  def getId: Int = id
}

private object Guest {
  private var id = 0

  private def getNewID: Int = {
    incrementID()
    id
  }

  private def incrementID(): Unit = id = id+1
}

object ConfirmationStatus extends Enumeration{
  type ConfirmationStatus = Value
  val PENDING, ATTENDING, NOT_ATTENDING = Value
}

case class GuestRequest(
   userId: Int,
   eventId: Int,
   confirmationStatus: ConfirmationStatus,
   isHost: Boolean,
){
  def getGuest: Guest = Guest(userId, eventId, confirmationStatus, isHost)
}
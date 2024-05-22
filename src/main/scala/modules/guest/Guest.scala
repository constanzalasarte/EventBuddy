package modules.guest

import ConfirmationStatus.ConfirmationStatus

case class Guest(
  private var userId: Int,
  private var eventId: Int,
  private var confirmationStatus: ConfirmationStatus,
  private var isHost: Boolean,
  private val id: Int = Guest.getNewID,
){
  def getUserId: Int = userId
  def getEventId: Int = eventId
  def getConfirmationStatus: ConfirmationStatus = confirmationStatus
  def getIsHost: Boolean = isHost
  def getId: Int = id

  def changeUserId(newUserId: Int): Unit = userId = newUserId
  def changeEventId(newEventId: Int): Unit = eventId = newEventId
  def changeConfirmationStatus(newConfirmationStatus: ConfirmationStatus): Unit =
    confirmationStatus = newConfirmationStatus
  def changeIsHost(newIsHost: Boolean): Unit = isHost = newIsHost
}

object Guest {
  private var id = 0

  def start(): Unit = id = 0

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

case class GuestPatchRequest(
  userId: Option[Int],
  eventId: Option[Int],
  confirmationStatus: Option[ConfirmationStatus],
  isHost: Option[Boolean],
){
  def hasUserId: Boolean = userId.isDefined
  def hasEventId: Boolean = eventId.isDefined
  def hasConfirmationStatus: Boolean = confirmationStatus.isDefined
  def hasIsHost: Boolean = isHost.isDefined
}
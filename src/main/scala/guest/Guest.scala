package guest

import guest.ConfirmationStatus.ConfirmationStatus

case class Guest(
  private val userId: Int,
  private val eventId: Int,
  private val confirmationStatus: ConfirmationStatus,
  private val isHost: Boolean,
  private val id: Int = Guest.getNewID,
){

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
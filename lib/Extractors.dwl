%dw 2.0
import * from bat::Mutable

fun setUserId(context, userId: Number) = context.set('userId', userId)
fun getUserId(context) : Number | Null = context.get('userId')

fun setEventId(context, eventId: Number) = context.set('eventId', eventId)
fun getEventId(context) : Number | Null = context.get('eventId')

fun setElementId(context, elementId: Number) = context.set('elementId', elementId)
fun getElementId(context) : Number | Null = context.get('elementId')

fun setGuestId(context, guestId: Number) = context.set('guestId', guestId)
fun getGuestId(context) : Number | Null = context.get('guestId')

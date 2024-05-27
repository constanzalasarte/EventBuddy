%dw 2.0
import * from bat::Mutable

fun setUserId(context, userId: Number) = context.set('userId', userId)
fun setEventId(context, eventId: Number) = context.set('eventId', eventId)
fun getUserId(context) : Number | Null = context.get('userId')
fun getEventId(context) : Number | Null = context.get('eventId')

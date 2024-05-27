%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::HTTPCodes
import * from lib::BasicRequest

var context = HashMap()

fun unprocessableGuest(userId: Number, eventId: Number, obj: String, wrongId: Number) =
    POST `$(config.url)/guest` with {
          body: {
              userId: userId,
              eventId: eventId,
              confirmationStatus: "PENDING",
              isHost: false,
          }
      } assert [
          $.response.status mustEqual UNPROCESSABLE_ENTITY,
          `There is no $(obj) with id $(wrongId)` mustEqual $.response.body
      ]


---
suite("add guest") in [
    it must 'answer CREATED when creating a user' in [
        createUser(context, config.url)
    ],
    it should 'answer UNPROCESSABLE ENTITY when creating a guest with an invalid event' in [
        unprocessableGuest(getUserId(context), -1, 'event', -1)
    ],
    it must 'answer CREATED when creating an event' in [
        createEvent(context, config.url)
    ],
    it should 'answer UNPROCESSABLE ENTITY when creating a guest with an invalid user' in [
        unprocessableGuest(-1, getEventId(context), 'user', -1)
    ],
    it should 'answer CREATED when creating a guest' in [
        createGuest(context, config.url)
    ],
    it must 'delete OK a user' in [
        deleteUser(getUserId(context), config.url)
    ],
    it should 'not get event when the user has been deleted' in [
        checkThatNotExist('event', getEventId(context), config.url)
    ],
]
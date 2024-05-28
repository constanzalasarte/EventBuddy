%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::HTTPCodes
import * from lib::BasicRequest

var context = HashMap()

fun unprocessableEvent(userId: Number) =
    POST `$(config.url)/event` with {
          body: {
              name: "event name",
              description: "event description",
              creatorId: userId,
              date: "2001.07.04 AD at 12:08:56 PDT"
          }
      } assert [
          $.response.status mustEqual UNPROCESSABLE_ENTITY,
          `There is no user with id $(userId)` mustEqual $.response.body
      ]
---
suite("add event") in [
    it should 'answer UNPROCESSABLE ENTITY when creating an event' in [
        unprocessableEvent(-1)
    ],
    given must 'answer CREATED when creating an user' in [
        createUser(context, config.url)
    ],
    it must 'answer CREATED when creating an event' in [
        createEvent(context, config.url)
    ],
    it should 'get event that has been created' in [
        getObj(getEventId(context), config.url, 'event')
    ],
    it must 'delete OK a user' in [
        deleteUser(getUserId(context), config.url)
    ],
    it should 'not get event when the user has been deleted' in [
        checkThatNotExist('event', getEventId(context), config.url)
    ],
]
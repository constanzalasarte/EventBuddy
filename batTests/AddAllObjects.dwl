%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::BasicRequest

var context = HashMap()
---
suite("add user, in  this suite the idea is to create a user, an event,
an element and a guest, and it should all be deleted when the user is deleted") in [
    it must 'answer CREATED when creating a user' in [
        createUser(context, config.url)
    ],
    it must 'answer CREATED when creating an event' in [
        createEvent(context, config.url)
    ],
     it must 'answer CREATED when creating an element' in [
        createElement(context, config.url)
    ],
    it must 'answer CREATED when creating an guest' in [
        createGuest(context, config.url)
    ],
    it must 'delete OK a user' in [
        deleteUser(getUserId(context), config.url)
    ],
    it must 'not get event when the user has been deleted' in [
        checkThatNotExist('event', getEventId(context), config.url)
    ],
    it must 'not get element when the event has been deleted' in [
        checkThatNotExist('element', getElementId(context), config.url)
    ],
    it must 'not get guest when the event has been deleted' in [
        checkThatNotExist('guest', getGuestId(context), config.url)
    ],
]

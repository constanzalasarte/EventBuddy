%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::HTTPCodes
import * from lib::BasicRequest

var context = HashMap()

---
suite("get guest") in [
    it must 'answer CREATED when creating a user' in [
        createUser(context, config.url)
    ],

    it must 'answer CREATED when creating an event' in [
        createEvent(context, config.url)
    ],

    it should 'answer CREATED when creating a guest' in [
        createGuest(context, config.url)
    ],

    it should 'get guest that has been created' in [
        getObj(getGuestId(context), config.url, 'guest')
    ],

    it should 'modify guest that has been created' in [
        getObj(getGuestId(context), config.url, 'guest')
    ],

    it must 'delete OK a user' in [
        deleteUser(getUserId(context), config.url)
    ],

    it must 'not get guest when the event has been deleted' in [
        checkThatNotExist('guest', getGuestId(context), config.url)
    ],
]

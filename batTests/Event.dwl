%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::BasicRequest

var context = HashMap()
---
suite("event") in [
    given must 'answer CREATED when creating an user' in [
        createUser(context, config.url)
    ],
    it must 'answer CREATED when creating an event' in [
        createEvent(context, config.url)
    ],
    it should 'get event that has been created' in [
        getObj(getEventId(context), config.url, 'event')
    ],

    it must 'answer OK when modifing an event' in [
        modifyObjName(getEventId(context), config.url, "new name", "event")
    ],

    it should 'get event that has been modified' in [
        getObjAndCheckAttribute(getEventId(context), config.url, "name", "new name", 'event')
    ],

    it must 'delete OK a user' in [
        deleteUser(getUserId(context), config.url)
    ],
    it should 'not get event when the user has been deleted' in [
        checkThatNotExist('event', getEventId(context), config.url)
    ],
]
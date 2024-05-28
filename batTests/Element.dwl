%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::BasicRequest

var context = HashMap()
---
suite("element") in [
    given must 'answer CREATED when creating an user and event' in [
        createUser(context, config.url),
        createEvent(context, config.url)
    ],
    it must 'answer CREATED when creating an event' in [
        createElement(context, config.url)
    ],
    it should 'get element that has been created' in [
        getObj(getElementId(context), config.url, 'element')
    ],

    it must 'answer OK when modifing an element' in [
        modifyObjName(getElementId(context), config.url, "new name", "element")
    ],

    it should 'get element that has been modified' in [
        getObjAndCheckAttribute(getElementId(context), config.url, "name", "new name", 'element')
    ],

    it must 'delete OK a user' in [
        deleteUser(getUserId(context), config.url)
    ],

    it must 'not get element when the event has been deleted' in [
        checkThatNotExist('element', getElementId(context), config.url)
    ],
]
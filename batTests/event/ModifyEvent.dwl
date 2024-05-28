%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::HTTPCodes
import * from lib::BasicRequest

var context = HashMap()

fun unprocessableEventWName(id: Number, attribute: String, value: String) =
    PUT `$(config.url)/event/byId?id=$(id)` with {
        body: {
            attribute: value
        }
    } assert [
        $.response.status mustEqual UNPROCESSABLE_ENTITY,
        `There is no event with id $(id)` mustEqual $.response.body
    ]

fun modifyEvent(id: Number, attribute: String, value: String) =
    PUT `$(config.url)/event/byId?id=$(id)` with {
        body: {
            attribute: value
        }
    } assert [
        $.response.status mustEqual OK,
    ]
---
suite("modify event") in [
    it should 'answer UNPROCESSABLE ENTITY when modifing an event' in [
        unprocessableEventWName(-1, "name", "event name")
    ],
    given must 'answer CREATED when creating an user and event' in [
        createUser(context, config.url),
        createEvent(context, config.url)
    ],
    it must 'answer OK when modifing an event' in [
        modifyEvent(getEventId(context), "name", "event name")
    ],

    it should 'get event that has been modified' in [
        getObjAndCheckAttribute(getEventId(context), config.url, 'event', "name", "event name"),
        GET `$(config.url)/event/byId?id=$(getEventId(context))` with {} assert [
            $.response.status mustEqual OK,
            $.response.body."name" mustEqual "event name"
        ]
    ],

    it must 'delete OK a user' in [
        deleteUser(getUserId(context), config.url)
    ],
]

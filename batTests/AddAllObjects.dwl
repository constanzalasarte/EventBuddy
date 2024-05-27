%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::HTTPCodes
import * from lib::BasicRequest

var context = HashMap()

fun checkThatNotExist(obj: String, id: String) =
    GET `$(config.url)/$(obj)/byId?id=$(id)` with {} assert [
        $.response.status mustEqual UNPROCESSABLE_ENTITY,
        `There is no $(obj) with id $(id)` mustEqual $.response.body
    ]
fun createGuest() =
    POST `$(config.url)/guest` with {
        body: {
            userId: getUserId(context),
            eventId: getEventId(context),
            confirmationStatus: 'PENDING',
            isHost: false,
        }
    } assert [
        $.response.status mustEqual CREATED
    ] execute [
        setGuestId(context, $.response.body.id)
    ]
fun createElement() =
    POST `$(config.url)/element` with {
        body: {
            name: "element name",
            quantity: 1,
            eventId: getEventId(context),
            maxUsers: 1,
            users: [getUserId(context)]
        }
    } assert [
        $.response.status mustEqual CREATED
    ] execute [
        setElementId(context, $.response.body.id)
    ]

fun deleteUser(id: Number) =
    DELETE `$(config.url)/user/byId?id=$(id)`
---
suite("add user, in  this suite the idea is to create a user, an event,
an element and a guest, and it should all be deleted when the user is deleted") in [
    it should 'answer CREATED when creating a user' in [
        POST `$(config.url)/user` with {
            body: {
                email: "user@mail.com",
                userName: "userName"
            }
        } assert [
            $.response.status mustEqual CREATED
        ] execute [
            setUserId(context, $.response.body.id)
        ]
    ],
    it must 'answer CREATED when creating an event' in [
        POST `$(config.url)/event` with {
            body: {
                name: "event name",
                description: "event description",
                creatorId: context.get('userId'),
                date: "2001.07.04 AD at 12:08:56 PDT"
            }
        } assert [
            $.response.status mustEqual CREATED
        ] execute [
            setEventId(context, $.response.body.id)
        ]
    ],
     it must 'answer CREATED when creating an element' in [
        createElement()
    ],
    it must 'answer CREATED when creating an guest' in [
        createGuest()
    ],
    it must 'delete OK a user' in [
        deleteUser(getUserId(context))
    ],
    it must 'not get event when the user has been deleted' in [
        checkThatNotExist('event', getEventId(context))
    ],
    it must 'not get element when the event has been deleted' in [
        checkThatNotExist('element', getElementId(context))
    ],
    it must 'not get guest when the event has been deleted' in [
        checkThatNotExist('guest', getGuestId(context))
    ],
]

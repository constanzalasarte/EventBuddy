%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable

var context = HashMap()
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
            $.response.status mustEqual 201
        ] execute [
            context.set('userId', $.response.body.id),
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
            $.response.status mustEqual 201
        ] execute [
            context.set('eventId', $.response.body.id),
        ]
    ],
     it must 'answer CREATED when creating an element' in [
        POST `$(config.url)/element` with {
            body: {
                name: "element name",
                quantity: 1,
                eventId: context.get('eventId'),
                maxUsers: 1,
                users: [context.get('userId')]
            }
        } assert [
            $.response.status mustEqual 201
        ] execute [
            context.set('elementId', $.response.body.id),
        ]
    ],
    it must 'answer CREATED when creating an guest' in [
        POST `$(config.url)/guest` with {
            body: {
                userId: context.get('userId'),
                eventId: context.get('eventId'),
                confirmationStatus: 'PENDING',
                isHost: false,
            }
        } assert [
            $.response.status mustEqual 201
        ] execute [
            context.set('guestId', $.response.body.id),
        ]
    ],
    it must 'delete OK a user' in [
        DELETE `$(config.url)/user/byId?id=$(context.get('userId'))` with {} assert [
            $.response.status mustEqual 200
        ]
    ],
    it should 'not get event when the user has been deleted' in [
        GET `$(config.url)/event/byId?id=$(context.get('eventId'))` with {} assert [
            $.response.status mustEqual 422,
            `There is no event with id $(context.get('eventId'))` mustEqual $.response.body
        ]
    ],
    it should 'not get element when the event has been deleted' in [
        GET `$(config.url)/element/byId?id=$(context.get('elementId'))` with {} assert [
            $.response.status mustEqual 422,
            `There is no element with id $(context.get('elementId'))` mustEqual $.response.body
        ]
    ],
    it should 'not get guest when the event has been deleted' in [
        GET `$(config.url)/guest/byId?id=$(context.get('guestId'))` with {} assert [
            $.response.status mustEqual 422,
            `There is no guest with id $(context.get('guestId'))` mustEqual $.response.body
        ]
    ],
]

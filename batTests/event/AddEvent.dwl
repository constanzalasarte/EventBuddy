%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable


var context = HashMap()
---
suite("add event") in [
    it should 'answer 404 when creating an event' in [
        POST `$(config.url)/event` with {
            body: {
                name: "event name",
                description: "event description",
                creatorId: -1,
                date: "2001.07.04 AD at 12:08:56 PDT"
            }
        } assert [
            $.response.status mustEqual 422,
            $.response.body mustEqual "There is no user with id -1"
        ]
    ],
    given must 'answer 200 when creating an user' in [
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
    it must 'answer 201 when creating an event' in [
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
    it should 'get event that has been created' in [
        GET `$(config.url)/event/byId?id=$(context.get('eventId'))` with {} assert [
            $.response.status mustEqual 200,
        ]
    ],
    it must 'delete 200 a user' in [
        DELETE `$(config.url)/event/byId?id=$(context.get('userId'))` with {} assert [
            $.response.status mustEqual 200
        ]
    ],
    it should 'not get event when the user has been deleted' in [
        GET `$(config.url)/event/byId?id=$(context.get('eventId'))` with {} assert [
            $.response.status mustEqual 422,
            `There is no event with id $(context.get('eventId'))` mustEqual $.response.body
        ]
    ],
]
%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::HTTPCodes

var context = HashMap()
---
suite("add event") in [
    it should 'answer UNPROCESSABLE ENTITY when creating an event' in [
        POST `$(config.url)/event` with {
            body: {
                name: "event name",
                description: "event description",
                creatorId: -1,
                date: "2001.07.04 AD at 12:08:56 PDT"
            }
        } assert [
            $.response.status mustEqual UNPROCESSABLE_ENTITY,
            $.response.body mustEqual "There is no user with id -1"
        ]
    ],
    given must 'answer OK when creating an user' in [
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
                creatorId: getUserId(context),
                date: "2001.07.04 AD at 12:08:56 PDT"
            }
        } assert [
            $.response.status mustEqual CREATED
        ] execute [
            setEventId(context, $.response.body.id)
        ]
    ],
    it should 'get event that has been created' in [
        GET `$(config.url)/event/byId?id=$(getEventId(context))` with {} assert [
            $.response.status mustEqual OK,
        ]
    ],
    it must 'delete OK a user' in [
        DELETE `$(config.url)/user/byId?id=$(getUserId(context))` with {} assert [
            $.response.status mustEqual OK
        ]
    ],
    it should 'not get event when the user has been deleted' in [
        GET `$(config.url)/event/byId?id=$(getEventId(context))` with {} assert [
            $.response.status mustEqual UNPROCESSABLE_ENTITY,
            `There is no event with id $(getEventId(context))` mustEqual $.response.body
        ]
    ],
]
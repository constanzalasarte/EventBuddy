%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from lib::Extractors
import * from lib::HTTPCodes


fun createUser(context, url) =
    POST `$(url)/user` with {
        body: {
            email: "user@mail.com",
            userName: "userName"
        }
    } assert [
        $.response.status mustEqual CREATED
    ] execute [
        setUserId(context, $.response.body.id)
    ]

fun createEvent(context, url) =
    POST `$(url)/event` with {
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

fun createElement(context, url) =
    POST `$(url)/element` with {
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

fun createGuest(context, url) =
    POST `$(url)/guest` with {
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


fun checkThatNotExist(obj: String, id: String, url: String) =
    GET `$(url)/$(obj)/$(id)` with {} assert [
        $.response.status mustEqual UNPROCESSABLE_ENTITY,
        `There is no $(obj) with id $(id)` mustEqual $.response.body
    ]

fun deleteUser(id: Number, url: String) =
    DELETE `$(url)/user/$(id)` with {} assert []

fun getObj(id: Number, url: String, obj: String) =
    GET `$(url)/$(obj)/$(id)` with {} assert [
        $.response.status mustEqual OK,
    ]

fun getObjAndCheckAttribute(id: Number, url: String, obj: String, attribute: String, value: String) =
    GET `$(url)/$(obj)/$(id)` with {} assert [
        $.response.status mustEqual OK,
        $.response.body[attribute] mustEqual value
    ]
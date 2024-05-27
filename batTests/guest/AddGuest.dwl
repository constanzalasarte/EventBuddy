%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::HTTPCodes

var context = HashMap()
---
suite("add guest") in [
    it must 'answer CREATED when creating a user' in [
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
   it should 'answer UNPROCESSABLE ENTITY when creating a guest with an invalid event' in [
      POST `$(config.url)/guest` with {
          body: {
              userId: getUserId(context),
              eventId: -1,
              confirmationStatus: "PENDING",
              isHost: false,
          }
      } assert [
          $.response.status mustEqual UNPROCESSABLE_ENTITY,
          $.response.body mustEqual "There is no event with id -1"
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
   it should 'answer UNPROCESSABLE ENTITY when creating a guest with an invalid user' in [
     POST `$(config.url)/guest` with {
         body: {
             userId: -1,
             eventId: getEventId(context),
             confirmationStatus: "PENDING",
             isHost: false,
         }
     } assert [
         $.response.status mustEqual UNPROCESSABLE_ENTITY,
         $.response.body mustEqual "There is no user with id -1"
     ]
  ],
  it should 'answer CREATED when creating a guest' in [
       POST `$(config.url)/guest` with {
           body: {
               userId: getUserId(context),
               eventId: getEventId(context),
               confirmationStatus: "PENDING",
               isHost: false,
           }
       } assert [
           $.response.status mustEqual CREATED
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
%dw 2.0
import * from bat::BDD
import * from bat::Assertions
---
suite("add event") in [
  it must 'answer 404 when creating an event' in [
    POST `$(config.url)` with {
        body: {
            name: "event name",
            description: "event description",
            creatorId: 1,
            date: "2001.07.04 AD at 12:08:56 PDT"
        }
    } assert [
        $.response.status mustEqual 404 /*Ok*/,
        $.response.body mustEqual "There is no user with id 1"
    ] execute [
        log($.response.body)
    ]
  ]
]
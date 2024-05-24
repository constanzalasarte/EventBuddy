%dw 2.0
import * from bat::BDD
import * from bat::Assertions
---
suite("get event") in [
  it must 'answer 200 with event by id' in [
    GET `$(config.url)/event/byId` with {} assert [
        $.response.status mustEqual 200 /*Ok*/,
        $.response.body mustEqual "/event/byId"
    ]
  ],
  it must 'answer 200 getting empty set of events' in [
        GET `$(config.url)/event` with {} assert [
            $.response.status mustEqual 200 /*Ok*/,
            $.response.body mustEqual []
        ]
    ]
]
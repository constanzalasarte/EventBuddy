%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from lib::Extractors
import * from lib::HTTPCodes
---
suite("get event") in [
  it must 'answer OK with event by id' in [
    GET `$(config.url)/event/byId` with {} assert [
        $.response.status mustEqual OK,
        $.response.body mustEqual "/event/byId"
    ]
  ],
  it must 'answer OK getting empty set of events' in [
        GET `$(config.url)/event` with {} assert [
            $.response.status mustEqual OK,
            $.response.body mustEqual []
        ]
    ]
]
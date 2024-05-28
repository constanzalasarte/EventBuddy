%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from lib::Extractors
import * from lib::HTTPCodes
---
suite("get event") in [
  it must 'answer OK getting empty set of events' in [
        GET `$(config.url)/event` with {} assert [
            $.response.status mustEqual OK,
            $.response.body mustEqual []
        ]
    ]
]
%dw 2.0
import * from bat::BDD
import * from bat::Assertions
---
suite("get event") in [
  it must 'answer 200' in [
    GET `$(config.url)` with {} assert [
      $.response.status mustEqual 200 /*Ok*/,
      $.response.body mustEqual "/event/byId"
    ] execute [
        log($.response.body)
    ]
  ]
]
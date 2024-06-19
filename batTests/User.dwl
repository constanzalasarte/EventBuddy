%dw 2.0
import * from bat::BDD
import * from bat::Assertions
import * from bat::Mutable
import * from lib::Extractors
import * from lib::BasicRequest

var context = HashMap()
---
suite("user") in [
  it must 'answer CREATED when creating an user' in [
      createUser(context, config.url)
  ],
  it should 'get user that has been created' in [
      getObj(getUserId(context), config.url, 'user')
  ],

  it must 'answer OK when modifing an user' in [
      modifyUserName(getUserId(context), config.url, "new name")
  ],

  it should 'get user that has been modified' in [
      getObjAndCheckAttribute(getUserId(context), config.url, "userName", "new name", 'user')
  ],

  it must 'delete OK a user' in [
      deleteUser(getUserId(context), config.url)
  ],
]
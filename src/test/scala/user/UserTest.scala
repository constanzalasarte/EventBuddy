package user

import org.scalatest.flatspec.AnyFlatSpec

class UserTest extends AnyFlatSpec{
  "Two users" should "be created and the first one must have id 1 and the second one id 2" in {
    val email = "user@mail.com"
    val userName = "user"
    val user1 = new User(email, userName)
    val user2 = new User(email, userName)
    assert(user1.getId != user2.getId)
  }

  "A user" should "be created" in {
    val email = "user@mail.com"
    val userName = "user"
    val user = new User(email, userName)
    assert(user.getEmail == email)
    assert(user.getUserName == userName)
  }
}

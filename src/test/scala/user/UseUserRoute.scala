package user

case class UseUserRoute(users: Users) {
  def createAUser(email: String, userName: String): User = {
    val userRequest = UserRequest(email, userName)
    val user = userRequest.getUser
    users.addUser(user)
    user
  }
}

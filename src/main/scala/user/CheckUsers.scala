package user

trait CheckUsers {
  def byID(id: Int): Option[User]
}

package modules.user

trait CheckUsers {
  def byID(id: Int): Option[User]
  def deleteById(id: Int): Boolean
}

package util

sealed abstract class Version
object Version {
  case object SetVersion extends Version
  case object DBVersion extends Version
}

package util.exceptions

case class IDNotFoundException(name: String, id: Int) extends Exception(s"There is no $name with id $id")

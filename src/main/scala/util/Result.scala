package util

import org.apache.pekko.http.scaladsl.model.{StatusCode, StatusCodes}
import util.exceptions.{IDNotFoundException, UnacceptableException}

sealed trait Result[T]{
  def getStatusCode: StatusCode
}
case class Ok[T](result: T) extends Result[T] {
  override def getStatusCode: StatusCode = StatusCodes.OK
}
case class Error[T](error: Throwable) extends Result[T] {
  override def getStatusCode: StatusCode = error match {
    case _: IDNotFoundException => StatusCodes.UnprocessableEntity
    case _: UnacceptableException => StatusCodes.NotAcceptable
  }
}
case class Created[T](result: T) extends Result[T] {
  override def getStatusCode: StatusCode = StatusCodes.Created
}

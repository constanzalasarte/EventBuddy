package modules.element.repository

import modules.element.controller.Element
import slick.dbio.{DBIO, DBIOAction}
import slick.jdbc.JdbcBackend.Database
import server.Server.executionContext
import slick.jdbc.PostgresProfile.api._
import util.DBTables
import util.DBTables.{ElementEntity, UserElementEntity}

import scala.concurrent.Future

case class ElementDBRepo(db: Database) extends ElementsRepository {
  private val elementTable = DBTables.elementTable
  private val userElement = DBTables.userElement
  override def addElement(element: Element): Future[Unit] = {
    val action = if(element.getUsers.nonEmpty)
      DBIO.seq(elementTable += transformElem(element), userElement ++= transformUserElem(element))
    else DBIO.seq(elementTable += transformElem(element))
    for {
      _ <- db.run(action)
    } yield {}
  }

  override def getElements: Future[Set[Element]] =
    for {
      elements <- db.run(elementTable.sortBy(_.id).result)
      userElem <- db.run(userElement.sortBy(_.id).result)
    } yield {
      transformToElemSet(elements, userElem)
    }

  private def transformToElemSet(elementsEntity: Seq[ElementEntity], userElemSeq: Seq[UserElementEntity]): Set[Element] = {
    val elements : Set[Element] = elementsEntity.toSet.map(
      elemEntity => transformElemEntity(elemEntity)
    )
    elements.foreach(elem =>
      userElemSeq.foreach(userElem =>
        if(userElem.elementId == elem.getId) elem.changeUsers(elem.getUsers + userElem.userId)
      )
    )
    elements
  }

  private def transformElemEntity(elemEntity: ElementEntity): Element =
    new Element(elemEntity.name, elemEntity.quantity, elemEntity.eventId, elemEntity.maxUsers, Set.empty, elemEntity.id.get)

  override def changeById(id: Int, newElem: Element): Future[Unit] = ???

  override def byId(id: Int): Future[Option[Element]] = ???

  override def deleteById(id: Int): Future[Unit] = ???

  private def transformUserElem(element: Element): Set[UserElementEntity] = {
    element.getUsers.map(user =>
      UserElementEntity(None, user, element.getId)
    )
  }

  private def transformElem(element: Element): ElementEntity = {
    ElementEntity(Some(element.getId), element.getName, element.getQty, element.getEventId, element.getMaxUsers)
  }
}

package modules.element.repository

import modules.element.controller.Element
import server.Server.executionContext

import scala.concurrent.Future


case class ElementsSetRepo(private var elements: Set[Element]) extends ElementsRepository {

  override def addElement(element: Element): Future[Unit] = {
    elements = elements + element
    Future { }
  }

  override def getElements: Future[Set[Element]] = Future { elements }

  override def changeById(id: Int, newElem: Element): Future[Unit] = {
    var result: Set[Element]= Set.empty
    for (elem <- elements) {
      if(elem.getId == id) result = result + newElem
      else result = result + elem
    }
    elements = result
    Future { }
  }

  override def byId(id: Int): Future[Option[Element]] = Future {
    elements.find(elem => elem.getId == id)
  }

  override def deleteById(id: Int): Future[Unit] = {
    val maybeElement = elements.find(_.getId == id)
    maybeElement.foreach { found =>
      elements = elements - found
    }
    Future { }
  }
}

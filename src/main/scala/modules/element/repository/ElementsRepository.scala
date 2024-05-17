package modules.element.repository

import modules.element.controller.Element

import scala.concurrent.Future

trait ElementsRepository {
  def addElement(element: Element): Future[Unit]

  def getElements: Future[Set[Element]]

  def changeById(id: Int, newElem: Element): Future[Unit]

  def byId(id: Int): Future[Option[Element]]

  def deleteById(id: Int): Future[Unit]
}

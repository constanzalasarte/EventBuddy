package modules.element.repository

import modules.element.controller.Element

trait ElementsRepository {
  def addElement(element: Element): Unit

  def getElements: Set[Element]

  def changeById(id: Int, newElem: Element): Unit

  def byId(id: Int): Option[Element]

  def deleteById(id: Int): Boolean
}

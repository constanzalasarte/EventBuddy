package element.repository

import element.Element

case class ElementsSetRepo(private var elements: Set[Element]) extends ElementsRepository {

  override def addElement(element: Element): Unit =
    elements = elements + element

  override def getElements: Set[Element] = elements

  override def changeById(id: Int, newElem: Element): Unit = {
    var result: Set[Element]= Set.empty
    for (elem <- elements) {
      if(elem.getId == id) result = result + newElem
      else result = result + elem
    }
    elements = result
  }

  override def byId(id: Int): Option[Element] =
    elements.find(elem => elem.getId == id)

  override def deleteById(id: Int): Boolean = {
    val maybeElement = elements.find(_.getId == id)
    maybeElement.foreach { found =>
      elements = elements - found
    }
    maybeElement.isDefined
  }
}

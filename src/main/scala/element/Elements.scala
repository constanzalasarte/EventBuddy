package element

case class Elements(private var elements: Set[Element]) extends CheckElements{
  def addElement(element: Element): Unit =
    elements = elements + element

  def getElements: Set[Element] = elements

  def changeById(id: Int, newElem: Element): Unit = {
    var result: Set[Element]= Set.empty
    for (elem <- elements) {
      if(elem.getId == id) result = result + newElem
      else result = result + elem
    }
    elements = result
  }

  override def byId(id: Int): Option[Element] = {
    for(elem <- elements){
      if(elem.getId == id) return Some(elem)
    }
    None
  }

  override def deleteById(id: Int): Boolean = {
    var result: Set[Element]= Set.empty
    var foundEvent: Boolean = false
    for (elem <- elements) {
      if(elem.getId == id) foundEvent = true
      else result = result + elem
    }
    if(foundEvent) elements = result
    foundEvent
  }

  override def deleteByEventId(id: Int): Unit = {
    var result: Set[Element]= Set.empty
    for (elem <- elements) {
      if(elem.getEventId != id) result = result + elem
    }
    elements = result
  }

  override def deleteUserInUsers(id: Int): Unit =
    elements.foreach(elem => elem.deleteUserInUsers(id))
}
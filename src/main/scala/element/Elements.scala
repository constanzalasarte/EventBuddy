package element

case class Elements(private var elements: Set[Element]) {
  def addElement(element: Element): Unit =
    elements = elements + element

  def getElements: Set[Element] = elements
}
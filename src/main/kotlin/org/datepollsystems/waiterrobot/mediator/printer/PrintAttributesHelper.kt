package org.datepollsystems.waiterrobot.mediator.printer

import javax.print.attribute.*

private fun addIfCategoryNotPresent(attributeSet: AttributeSet, attribute: Attribute) {
    if (attributeSet.containsKey(attribute.category)) return
    attributeSet.add(attribute)
}

// Ensure only right types can be added
fun DocAttributeSet.addIfCategoryNotPresent(attribute: DocAttribute) = addIfCategoryNotPresent(this, attribute)
fun PrintRequestAttributeSet.addIfCategoryNotPresent(attribute: PrintRequestAttribute) =
    addIfCategoryNotPresent(this, attribute)
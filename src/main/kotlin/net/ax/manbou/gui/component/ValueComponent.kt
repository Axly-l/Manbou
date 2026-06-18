package net.ax.manbou.gui.component

import net.ax.manbou.gui.ComponentBase
import net.ax.manbou.module.Value

abstract class ValueComponent(
    val value: Value<*>,
    x: Int,
    y: Int,
    width: Int,
    height: Int = 14
) : ComponentBase(x, y, width, height) {
    val visible: Boolean
        get() = value.visibility()
    open val totalHeight: Int get() = height
}

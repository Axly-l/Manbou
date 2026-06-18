package net.ax.manbou.gui

abstract class ComponentBase(
    var x: Int,
    var y: Int,
    open var width: Int,
    open var height: Int
) {
    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean = false
    open fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {}
    open fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {}
    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean {return false}

    open fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
    }
}
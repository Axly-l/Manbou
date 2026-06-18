package net.ax.manbou.gui.component

import net.ax.manbou.gui.GuiUtils
import net.ax.manbou.module.BooleanValue

class BooleanValueComponent(
    private val boolValue: BooleanValue,
    x: Int,
    y: Int,
    width: Int
) : ValueComponent(boolValue, x, y, width, 14) {

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        val isHovered = isMouseOver(mouseX, mouseY)
        val bgColor = if (isHovered) GuiUtils.getColor(50, 50, 50, 220) else GuiUtils.getColor(30, 30, 30, 220)
        GuiUtils.drawRect(x, y, width, height, bgColor)

        val indicatorColor = if (boolValue.value) GuiUtils.getColor(60, 200, 100) else GuiUtils.getColor(100, 100, 100)
        GuiUtils.drawRect(x + width - 10, y + 3, 7, 8, indicatorColor)

        val textColor = GuiUtils.getColor(200, 200, 200)
        GuiUtils.drawString(value.name, x + 4, y + 3, textColor, true)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!visible) return false
        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            boolValue.toggle()
            return true
        }
        return false
    }
}

package net.ax.manbou.gui.component

import net.ax.manbou.gui.GuiUtils
import net.ax.manbou.module.ListValue

class ListValueComponent(
    private val listValue: ListValue,
    x: Int,
    y: Int,
    width: Int
) : ValueComponent(listValue, x, y, width, 14) {
    private val optionHeight = 12

    override val totalHeight: Int
        get() = if (listValue.expanded) height + listValue.options.size * optionHeight else height

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        val isHovered = isMouseOver(mouseX, mouseY)
        val bgColor = if (isHovered) GuiUtils.getColor(50, 50, 50, 220) else GuiUtils.getColor(30, 30, 30, 220)
        GuiUtils.drawRect(x, y, width, height, bgColor)

        val arrow = if (listValue.expanded) "-" else "+"
        val textColor = GuiUtils.getColor(200, 200, 200)
        GuiUtils.drawString("${value.name}: ${listValue.stringValue} $arrow", x + 4, y + 3, textColor, true)

        if (listValue.expanded && visible) {
            for (i in listValue.options.indices) {
                val optionY = y + height + i * optionHeight
                val optionHovered = mouseX >= x && mouseX <= x + width && mouseY >= optionY && mouseY < optionY + optionHeight
                val optionBg = if (optionHovered) GuiUtils.getColor(60, 60, 60, 240) else GuiUtils.getColor(25, 25, 25, 240)
                GuiUtils.drawRect(x, optionY, width, optionHeight, optionBg)

                val isSelected = i == listValue.value
                val optionTextColor = if (isSelected) GuiUtils.getColor(80, 180, 255) else GuiUtils.getColor(180, 180, 180)
                GuiUtils.drawString(listValue.options[i], x + 6, optionY + 2, optionTextColor, true)
            }
        }
    }

    override fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
        val baseHover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height
        if (listValue.expanded) {
            val inDropdown = mouseX >= x && mouseX <= x + width && mouseY >= y + height && mouseY < y + totalHeight
            return baseHover || inDropdown
        }
        return baseHover
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!visible) return false

        if (mouseButton == 0) {
            if (!listValue.expanded && isMouseOver(mouseX, mouseY)) {
                listValue.next()
                return true
            }
            if (listValue.expanded) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= y + height && mouseY < y + totalHeight) {
                    val idx = (mouseY - y - height) / optionHeight
                    if (idx in listValue.options.indices) {
                        listValue.value = idx
                    }
                    return true
                }
            }
        }

        if(mouseButton == 1 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height) {
            listValue.expanded = !listValue.expanded
            return true
        }
        return false
    }
}

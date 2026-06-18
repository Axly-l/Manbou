package net.ax.manbou.gui.component

import net.ax.manbou.gui.GuiUtils
import net.ax.manbou.module.MultiValue

class MultiValueComponent(
    private val multiValue: MultiValue,
    x: Int,
    y: Int,
    width: Int
) : ValueComponent(multiValue, x, y, width, 14) {
    private val optionHeight = 12

    override val totalHeight: Int
        get() = if (multiValue.expanded) height + multiValue.options.size * optionHeight else height

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        val isHovered = isMouseOver(mouseX, mouseY)
        val bgColor = if (isHovered) GuiUtils.getColor(50, 50, 50, 220) else GuiUtils.getColor(30, 30, 30, 220)
        GuiUtils.drawRect(x, y, width, height, bgColor)

        val arrow = if (multiValue.expanded) "-" else "+"
        val textColor = GuiUtils.getColor(200, 200, 200)
        GuiUtils.drawString("${value.name} $arrow", x + 4, y + 3, textColor, true)

        if (multiValue.expanded) {
            for (i in multiValue.options.indices) {
                val optionY = y + height + i * optionHeight
                val optionHovered = mouseX >= x && mouseX <= x + width && mouseY >= optionY && mouseY < optionY + optionHeight
                val optionBg = if (optionHovered) GuiUtils.getColor(60, 60, 60, 240) else GuiUtils.getColor(25, 25, 25, 240)
                GuiUtils.drawRect(x, optionY, width, optionHeight, optionBg)

                val isSelected = multiValue.isSelected(i)
                val checkboxColor = if (isSelected) GuiUtils.getColor(60, 200, 100) else GuiUtils.getColor(80, 80, 80)
                GuiUtils.drawRect(x + 4, optionY + 3, 6, 6, checkboxColor)

                val optionTextColor = if (isSelected) GuiUtils.getColor(220, 220, 220) else GuiUtils.getColor(150, 150, 150)
                GuiUtils.drawString(multiValue.options[i], x + 14, optionY + 2, optionTextColor, true)
            }
        }
    }

    override fun isMouseOver(mouseX: Int, mouseY: Int): Boolean {
        val baseHover = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height
        if (multiValue.expanded) {
            val inDropdown = mouseX >= x && mouseX <= x + width && mouseY >= y + height && mouseY < y + totalHeight
            return baseHover || inDropdown
        }
        return baseHover
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!visible) return false
        if (mouseButton == 0 && multiValue.expanded && mouseX >= x && mouseX <= x + width && mouseY >= y + height && mouseY < y + totalHeight) {
            val idx = (mouseY - y - height) / optionHeight
            if (idx in multiValue.options.indices) {
                multiValue.toggle(idx)
            }
            return true
        }

        if (mouseButton == 1 && mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + height) {
            multiValue.expanded = !multiValue.expanded
            return true
        }
        return false
    }
}

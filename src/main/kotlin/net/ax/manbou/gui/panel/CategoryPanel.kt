package net.ax.manbou.gui.panel

import net.ax.manbou.gui.ComponentBase
import net.ax.manbou.gui.GuiUtils
import net.ax.manbou.gui.component.ModuleButton
import net.ax.manbou.module.Category
import net.ax.manbou.module.ModuleManager
import net.minecraft.client.Minecraft

class CategoryPanel(
    val category: Category,
    x: Int,
    y: Int,
    override var width: Int
) : ComponentBase(x, y, 0, 0) {
    private val moduleButtons = mutableListOf<ModuleButton>()
    private var isDragging = false
    private var dragOffsetX = 0
    private var dragOffsetY = 0
    private var expanded = true
    var scrollOffset = 0

    private val headerColor: Int = when (category) {
        Category.COMBAT -> GuiUtils.getColor(255, 60, 60)
        Category.MOVEMENT -> GuiUtils.getColor(60, 180, 255)
        Category.PLAYER -> GuiUtils.getColor(60, 255, 120)
        Category.WORLD -> GuiUtils.getColor(255, 200, 60)
        Category.VISUAL -> GuiUtils.getColor(180, 60, 255)
        Category.TARGET -> GuiUtils.getColor(255, 140, 60)
        Category.CLIENT -> GuiUtils.getColor(255, 60, 180)
        Category.NONE -> GuiUtils.getColor(180, 180, 180)
    }

    private val HEADER_HEIGHT = 16
    private val BUTTON_WIDTH: Int
        get() = width
    private val BUTTON_HEIGHT = 14

    init {
        rebuildButtons()
    }

    fun rebuildButtons() {
        moduleButtons.clear()
        val modules = ModuleManager.getByCategory(category)
            .filter { it.category != Category.NONE }

        var yOffset = HEADER_HEIGHT
        for (module in modules) {
            val btn = ModuleButton(module, x, y + yOffset, width)
            moduleButtons.add(btn)
            yOffset += btn.effectiveHeight
        }
        height = HEADER_HEIGHT + moduleButtons.sumOf { it.effectiveHeight }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        width = width.coerceAtLeast(100)

        GuiUtils.drawRect(x, y, width, HEADER_HEIGHT, headerColor)
        val arrow = if (expanded) "-" else "+"
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("${category.displayName} ${arrow} (${moduleButtons.size})", (x + 4).toFloat(), (y + 4).toFloat(), GuiUtils.getColor(255, 255, 255))

        if (expanded) {
            val currentY = y + HEADER_HEIGHT
            var yOffset = 0
            for (btn in moduleButtons) {
                btn.x = x
                btn.y = currentY + yOffset
                btn.width = width
                btn.drawScreen(mouseX, mouseY, partialTicks)
                yOffset += btn.effectiveHeight
            }
            height = HEADER_HEIGHT + yOffset
        } else {
            height = HEADER_HEIGHT
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHeaderMouseOver(mouseX, mouseY)) {
            if (expanded && mouseY < y + HEADER_HEIGHT) {
                isDragging = true
                dragOffsetX = mouseX - x
                dragOffsetY = mouseY - y
                return true
            }

            if (mouseY >= y && mouseY < y + HEADER_HEIGHT) {
                expanded = !expanded
                return true
            }
        }

        if (expanded && mouseButton == 1 && isHeaderMouseOver(mouseX, mouseY)) {
            for (btn in moduleButtons) {
                btn.mouseClicked(mouseX, mouseY, 1)
            }
            return true
        }

        if (expanded) {
            for (btn in moduleButtons) {
                if (btn.mouseClicked(mouseX, mouseY, mouseButton)) return true
            }
        }

        return false
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        isDragging = false
        for (btn in moduleButtons) {
            btn.mouseReleased(mouseX, mouseY, state)
        }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (isDragging) {
            x = (mouseX - dragOffsetX).coerceAtLeast(0)
            y = (mouseY - dragOffsetY).coerceAtLeast(0)
        }
        for (btn in moduleButtons) {
            btn.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        var flag = false
        for (btn in moduleButtons) {
            flag = flag || btn.keyTyped(typedChar, keyCode)
        }

        return flag
    }

    fun getModuleButtons(): List<ModuleButton> = moduleButtons.toList()

    fun isHeaderMouseOver(mouseX: Int, mouseY: Int): Boolean {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY < y + HEADER_HEIGHT
    }
}

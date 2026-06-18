package net.ax.manbou.gui

import net.ax.manbou.gui.panel.CategoryPanel
import net.ax.manbou.module.Category
import net.ax.manbou.module.ModuleManager
import net.ax.manbou.module.impl.visual.ClickGui
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class ClickGuiScreen : GuiScreen() {
    private val panels = mutableListOf<CategoryPanel>()

    override fun initGui() {
        panels.clear()
        val panelWidth = 110
        val spacing = 10
        var px = 10
        val py = 10

        for (category in Category.entries) {
            if (category == Category.NONE) continue
            val modules = ModuleManager.getModulesByCategory(category)
            if (modules.isEmpty()) continue
            val panel = CategoryPanel(category, px, py, panelWidth)
            panels.add(panel)
            px += panelWidth + spacing
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        for (panel in panels) panel.drawScreen(mouseX, mouseY, partialTicks)
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        bringPanelToFront(mouseX, mouseY)
        for (panel in panels.reversed()) {
            if (panel.mouseClicked(mouseX, mouseY, mouseButton)) break
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (panel in panels) panel.mouseReleased(mouseX, mouseY, state)
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        for (panel in panels) panel.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        var flag = false
        for (panel in panels) flag = flag || panel.keyTyped(typedChar, keyCode)
        if(flag) return

        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null)
            return
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val wheel = Mouse.getEventDWheel()
        if (wheel != 0) {
            val mouseX = Mouse.getEventX() * width / mc.displayWidth
            val mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1
            for (panel in panels.reversed()) {
                if (panel.isMouseOver(mouseX, mouseY)) break
            }
        }
    }

    override fun doesGuiPauseGame(): Boolean = false

    private fun bringPanelToFront(mouseX: Int, mouseY: Int) {
        for (i in panels.indices.reversed()) {
            val panel = panels[i]
            if (panel.isHeaderMouseOver(mouseX, mouseY)) {
                panels.removeAt(i)
                panels.add(panel)
                break
            }
        }
    }

    override fun onGuiClosed() {
        ClickGui.setState(false)
    }
}
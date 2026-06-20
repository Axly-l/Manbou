package net.ax.manbou.gui.component

import net.ax.manbou.gui.ComponentBase
import net.ax.manbou.gui.GuiUtils
import net.ax.manbou.module.*
import org.lwjgl.input.Keyboard

class ModuleButton(
    private val module: IModule,
    x: Int,
    y: Int,
    width: Int
) : ComponentBase(x, y, width, 16) {
    private var expanded = false
    private var settingKeyBind = false
    private val valueButtons = mutableListOf<ValueComponent>()
    var isDraggingSlider = false

    override var height: Int
        get() = 16
        set(_) {}

    val effectiveHeight: Int
        get() = if (expanded) 16 + 16 + valueButtons.sumOf { if(it.visible) it.totalHeight else 0 } else 16

    private val categoryColor: Int = when (module.category) {
        Category.COMBAT -> GuiUtils.getColor(255, 60, 60)
        Category.MOVEMENT -> GuiUtils.getColor(60, 180, 255)
        Category.PLAYER -> GuiUtils.getColor(60, 255, 120)
        Category.WORLD -> GuiUtils.getColor(255, 200, 60)
        Category.VISUAL -> GuiUtils.getColor(180, 60, 255)
        Category.TARGET -> GuiUtils.getColor(255, 140, 60)
        Category.CLIENT -> GuiUtils.getColor(255, 60, 180)
        Category.NONE -> GuiUtils.getColor(180, 180, 180)
    }

    init {
        rebuildValueButtons()
    }

    private fun rebuildValueButtons() {
        valueButtons.clear()
        var yOffset = 0

        if (expanded) {
            for (value in module.getDisplayValues()) {
                val comp = when (value) {
                    is BooleanValue -> BooleanValueComponent(value, 0, yOffset, width)
                    is IntValue -> NumberValueComponent(value, 0, yOffset, width) { isDraggingSlider = true }
                    is FloatValue -> NumberValueComponent(value, 0, yOffset, width) { isDraggingSlider = true }
                    is DoubleValue -> NumberValueComponent(value, 0, yOffset, width) { isDraggingSlider = true }
                    is ListValue -> ListValueComponent(value, 0, yOffset, width)
                    is MultiValue -> MultiValueComponent(value, 0, yOffset, width)
                    is StringValue -> StringValueComponent(value, 0, yOffset, width)
                    else -> continue
                }

                valueButtons.add(comp)
                yOffset += comp.totalHeight
            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        y = y.coerceAtLeast(0)

        val isHovered = isMouseOver(mouseX, mouseY)
        val bgColor = if (isHovered) GuiUtils.getColor(60, 60, 60, 200) else GuiUtils.getColor(40, 40, 40, 200)
        GuiUtils.drawRect(x, y, width, 16, bgColor)

        if (module.state) {
            GuiUtils.drawRect(x, y, 3, 16, categoryColor)
        }

        val statusText = if (module.state) "[ON]" else "[OFF]"
        val statusWidth = GuiUtils.getStringWidth(statusText)
        GuiUtils.drawString(statusText, x + width - statusWidth - 4, y + 4, if (module.state) categoryColor else GuiUtils.getColor(150, 150, 150), true)
        GuiUtils.drawString(module.name, x + 6, y + 4, GuiUtils.getColor(255, 255, 255), true)

        if (expanded) {
            // KeyBind行を描画
            val keyBindY = y + 16
            val keyBindBg = if (isMouseOverKeyBind(mouseX, mouseY)) GuiUtils.getColor(80, 80, 80, 200) else GuiUtils.getColor(50, 50, 50, 200)
            GuiUtils.drawRect(x, keyBindY, width, 16, keyBindBg)
            
            val keyBindDisplay = if (settingKeyBind) "KeyBind: ... (press key)" else {
                val keyText = if (module.keyBind == 0) "NONE" else if (module.keyBind < 0) "BUTTON ${-module.keyBind - 1}" else Keyboard.getKeyName(module.keyBind) ?: "KEY ${module.keyBind}"
                "KeyBind: $keyText"
            }
            val keyBindColor = if (settingKeyBind) GuiUtils.getColor(255, 255, 0) else GuiUtils.getColor(200, 200, 200)
            GuiUtils.drawString(keyBindDisplay, x + 6, keyBindY + 4, keyBindColor, true)

            var currentY = keyBindY + 16
            for (comp in valueButtons) {
                if (!comp.visible) continue
                comp.x = x
                comp.y = currentY
                comp.width = width
                comp.drawScreen(mouseX, mouseY, partialTicks)
                currentY += comp.totalHeight
            }
        }
    }

    private fun isMouseOverKeyBind(mouseX: Int, mouseY: Int): Boolean {
        if (!expanded) return false
        return mouseX in x until (x + width) && mouseY in (y + 16) until (y + 32)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (expanded) {
            if (isMouseOverKeyBind(mouseX, mouseY)) {
                settingKeyBind = true
                return true
            }

            for (comp in valueButtons) {
                if (comp.mouseClicked(mouseX, mouseY, mouseButton)) return true
            }
        }

        if (isMouseOver(mouseX, mouseY) && mouseButton == 0) {
            module.toggle()
            return true
        }

        if (isMouseOver(mouseX, mouseY) && mouseButton == 1) {
            expanded = !expanded
            rebuildValueButtons()
            return true
        }

        return false
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (isDraggingSlider) {
            isDraggingSlider = false
        }
        for (comp in valueButtons) {
            comp.mouseReleased(mouseX, mouseY, state)
        }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (isDraggingSlider) {
            for (comp in valueButtons) {
                comp.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (settingKeyBind) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                module.keyBind = 0
            } else if (keyCode != Keyboard.KEY_DELETE) {
                module.keyBind = keyCode
            }
            settingKeyBind = false
            return true
        }

        var flag = false
        for (comp in valueButtons) {
            flag = flag || comp.keyTyped(typedChar, keyCode)
        }

        return flag
    }
}

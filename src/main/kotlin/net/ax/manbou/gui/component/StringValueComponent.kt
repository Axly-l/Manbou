package net.ax.manbou.gui.component

import net.ax.manbou.gui.GuiUtils
import net.ax.manbou.module.StringValue
import org.lwjgl.input.Keyboard

class StringValueComponent(
    private val stringValue: StringValue,
    x: Int,
    y: Int,
    width: Int
) : ValueComponent(stringValue, x, y, width, 14) {
    private var isEditing = false
    private var editBuffer = ""
    private var cursorPos = 0

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        
        val isHovered = isMouseOver(mouseX, mouseY)
        val bgColor = if (isHovered) GuiUtils.getColor(50, 50, 50, 220) else GuiUtils.getColor(30, 30, 30, 220)
        GuiUtils.drawRect(x, y, width, height, bgColor)

        val displayText = if (isEditing) editBuffer else "${value.name}: ${stringValue.value}"
        val textColor = GuiUtils.getColor(200, 200, 200)
        GuiUtils.drawString(displayText, x + 4, y + 3, textColor, true)

        // カーソル描画 (点滅)
        if (isEditing && System.currentTimeMillis() % 1000 < 500) {
            val fontWidth = GuiUtils.getStringWidth(displayText.substring(0, cursorPos.coerceAtMost(displayText.length)))
            GuiUtils.drawString("|", x + 4 + fontWidth, y + 3, GuiUtils.getColor(255, 255, 255), true)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!visible) return false
        if (!isMouseOver(mouseX, mouseY)) return false

        if (mouseButton == 0) {
            isEditing = !isEditing
            if (isEditing) {
                editBuffer = stringValue.value
                cursorPos = editBuffer.length
            }
            return true
        }

        return false
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (!isEditing) return false

        when (keyCode) {
            Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER -> {
                applyEditBuffer()
                isEditing = false
                return true
            }
            Keyboard.KEY_ESCAPE -> {
                isEditing = false
                return true
            }
            Keyboard.KEY_LEFT -> {
                cursorPos = (cursorPos - 1).coerceAtLeast(0)
                return true
            }
            Keyboard.KEY_RIGHT -> {
                cursorPos = (cursorPos + 1).coerceAtMost(editBuffer.length)
                return true
            }
            Keyboard.KEY_BACK -> {
                if (editBuffer.isNotEmpty() && cursorPos > 0) {
                    editBuffer = editBuffer.removeRange(cursorPos - 1, cursorPos)
                    cursorPos--
                }
                return true
            }
            Keyboard.KEY_DELETE -> {
                if (editBuffer.isNotEmpty() && cursorPos < editBuffer.length) {
                    editBuffer = editBuffer.removeRange(cursorPos, cursorPos + 1)
                }
                return true
            }
        }

        // 印字可能な文字の入力
        if (typedChar >= 32.toChar() && typedChar != 127.toChar()) { // printable ASCII
            if (editBuffer.length >= stringValue.maxLength) return true
            
            if (cursorPos < editBuffer.length) {
                editBuffer = editBuffer.substring(0, cursorPos) + typedChar + editBuffer.substring(cursorPos)
            } else {
                editBuffer += typedChar
            }
            cursorPos++
        }

        return true
    }

    private fun applyEditBuffer() {
        stringValue.value = editBuffer
    }
}
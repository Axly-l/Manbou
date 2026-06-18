package net.ax.manbou.gui.component

import net.ax.manbou.gui.GuiUtils
import net.ax.manbou.module.DoubleValue
import net.ax.manbou.module.FloatValue
import net.ax.manbou.module.IntValue
import net.ax.manbou.module.NumberValue
import org.lwjgl.input.Keyboard
import kotlin.math.floor
import kotlin.math.roundToInt

class NumberValueComponent<T>(
    private val numValue: NumberValue<T>,
    x: Int,
    y: Int,
    width: Int,
    private val onSliderDragStart: () -> Unit = {}
) : ValueComponent(numValue, x, y, width, 14) where T : Number, T : Comparable<T> {
    private var isDragging = false
    private var isEditing = false
    private var editBuffer = ""
    private var cursorPos = 0

    private val rangeStart: Double = numValue.valueRange.start.toDouble()
    private val rangeEnd: Double = numValue.valueRange.endInclusive.toDouble()
    private var currentDouble: Double = numValue.value.toDouble()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!visible) return
        currentDouble = numValue.value.toDouble()

        val isHovered = isMouseOver(mouseX, mouseY)
        val bgColor = if (isHovered) GuiUtils.getColor(50, 50, 50, 220) else GuiUtils.getColor(30, 30, 30, 220)
        GuiUtils.drawRect(x, y, width, height, bgColor)

        val fillRatio = ((currentDouble - rangeStart) / (rangeEnd - rangeStart)).coerceIn(0.0, 1.0)
        val fillWidth = ((width - 4) * fillRatio).toInt()
        GuiUtils.drawRect(x + 2, y + height - 3, fillWidth.coerceAtLeast(0), 2, GuiUtils.getColor(80, 160, 255))

        val displayText = if (isEditing) editBuffer else "${value.name}: ${numValue.value}${numValue.suffix}"
        val textColor = GuiUtils.getColor(200, 200, 200)
        GuiUtils.drawString(displayText, x + 4, y + 3, textColor, true)

        // カーソル描画 (下線スタイル)
        if (isEditing && System.currentTimeMillis() % 1000 < 500) {
            val fontWidth = GuiUtils.getStringWidth(displayText.substring(0, cursorPos.coerceAtMost(displayText.length)))
            GuiUtils.drawString("_", x + 4 + fontWidth, y + 3, GuiUtils.getColor(255, 255, 255), true)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!visible) return false
        if (!isMouseOver(mouseX, mouseY)) return false

        if (mouseButton == 0) {
            if (isEditing) {
                isEditing = false
                return true
            }
            isDragging = true
            updateValueFromMouse(mouseX)
            onSliderDragStart()
            return true
        }

        if (mouseButton == 1) {
            isEditing = !isEditing
            if (isEditing) {
                editBuffer = numValue.value.toString()
                cursorPos = editBuffer.length
            }
            return true
        }

        return false
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (isDragging && state == 0) {
            isDragging = false
        }
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        if (isDragging) {
            updateValueFromMouse(mouseX)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (!isEditing) return false

        if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
            try {
                numValue.parse(editBuffer)
            } catch (_: Exception) {
                // ignored
            }
            isEditing = false
            return true
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            isEditing = false
            return true
        }

        if (keyCode == Keyboard.KEY_LEFT) {
            cursorPos = (cursorPos - 1).coerceAtLeast(0)
            return true
        }

        if (keyCode == Keyboard.KEY_RIGHT) {
            cursorPos = (cursorPos + 1).coerceAtMost(editBuffer.length)
            return true
        }

        if (keyCode == Keyboard.KEY_BACK) {
            if (editBuffer.isNotEmpty() && cursorPos > 0) {
                editBuffer = editBuffer.removeRange(cursorPos - 1, cursorPos)
                cursorPos--
            }
            return true
        }

        if (keyCode == Keyboard.KEY_DELETE) {
            if (editBuffer.isNotEmpty() && cursorPos < editBuffer.length) {
                editBuffer = editBuffer.removeRange(cursorPos, cursorPos + 1)
            }
            return true
        }

        if (typedChar.isDigit() || typedChar == '.' || typedChar == '-') {
            if (cursorPos < editBuffer.length) {
                editBuffer = editBuffer.substring(0, cursorPos) + typedChar + editBuffer.substring(cursorPos)
            } else {
                editBuffer += typedChar
            }
            cursorPos++
        }

        return true
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateValueFromMouse(mouseX: Int) {
        val ratio = ((mouseX - x - 2).toDouble() / (width - 4)).coerceIn(0.0, 1.0)
        val newValueDouble = rangeStart + (rangeEnd - rangeStart) * ratio

        numValue.value = when (numValue) {
            is IntValue -> newValueDouble.roundToInt() as T
            is FloatValue -> newValueDouble.toFloat() as T
            is DoubleValue -> newValueDouble as T
            else -> newValueDouble as T
        }
    }
}

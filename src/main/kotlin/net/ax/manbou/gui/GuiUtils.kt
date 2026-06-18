package net.ax.manbou.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import org.lwjgl.opengl.GL11

object GuiUtils {
    private val mc: Minecraft
        get() = Minecraft.getMinecraft()

    val fontRenderer: FontRenderer
        get() = mc.fontRendererObj

    fun drawRect(x: Int, y: Int, width: Int, height: Int, color: Int) {
        Gui.drawRect(x, y, x + width, y + height, color)
    }

    fun drawBorderedRect(x: Int, y: Int, width: Int, height: Int, borderWidth: Int, borderColor: Int, fillColor: Int) {
        Gui.drawRect(x, y, x + width, y + height, fillColor)
        if (borderWidth > 0) {
            Gui.drawRect(x, y, x + borderWidth, y + height, borderColor)
            Gui.drawRect(x + width - borderWidth, y, x + width, y + height, borderColor)
            Gui.drawRect(x + borderWidth, y, x + width - borderWidth, y + borderWidth, borderColor)
            Gui.drawRect(x + borderWidth, y + height - borderWidth, x + width - borderWidth, y + height, borderColor)
        }
    }

    fun drawString(text: String, x: Int, y: Int, color: Int, shadow: Boolean = true) {
        if (shadow) {
            fontRenderer.drawStringWithShadow(text, x.toFloat(), y.toFloat(), color)
        } else {
            fontRenderer.drawString(text, x, y, color)
        }
    }

    fun getStringWidth(text: String): Int = fontRenderer.getStringWidth(text)

    fun enableGLSmooth() {
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(1.0f)
    }

    fun disableGLSmooth() {
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
    }

    fun getColor(r: Int, g: Int, b: Int, a: Int = 255): Int {
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}

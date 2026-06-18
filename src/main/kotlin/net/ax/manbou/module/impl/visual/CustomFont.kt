package net.ax.manbou.module.impl.visual

import net.ax.manbou.font.CustomFontManager
import net.ax.manbou.module.*
import net.ax.manbou.utils.LogUtil

object CustomFont : ModuleBase("CustomFont", Category.VISUAL, defaultState = false) {
    private val fontList = mutableListOf("default").apply {
        addAll(CustomFontManager.getAvailableFonts())
    }

    val fontName: ListValue = ListValue("Font", fontList, 0) {
        if(this.state) replaceFontRenderer(fontName.options[it])
    }
    val fontSize = IntValue("FontSize", 36, 24..48, "pt") {
        if(this.state) replaceFontRenderer(fontName.stringValue, it)
    }
    val yOffset = IntValue("Y-Offset", 0, -10..10, "pt")

    var originalFontRenderer: net.minecraft.client.gui.FontRenderer? = null
        private set

    override fun onEnable() {
        replaceFontRenderer(fontName.stringValue)
    }

    override fun onDisable() {
        restoreFontRenderer()
    }

    private fun replaceFontRenderer(selectedFont: String, size: Int = 0) {
        val fontSize = if(size != 0) size else fontSize.value
        if (selectedFont == "default") {
            restoreFontRenderer()
            return
        }

        val customRenderer = CustomFontManager. createCustomFontRenderer(selectedFont, fontSize.toFloat())
        if (customRenderer != null) {
            if (originalFontRenderer == null) {
                originalFontRenderer = mc.fontRendererObj
            }
            mc.fontRendererObj = customRenderer
            LogUtil.info("Replaced global font renderer with: $selectedFont (${fontSize}pt)")
        } else {
            LogUtil.warn("Failed to create custom font renderer for: $selectedFont")
        }
    }

    private fun restoreFontRenderer() {
        originalFontRenderer?.let {
            mc.fontRendererObj = it
            originalFontRenderer = null
            LogUtil.info("Restored original font renderer")
        }
    }
}

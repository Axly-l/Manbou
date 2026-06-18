package net.ax.manbou.font

import net.ax.manbou.utils.LogUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.settings.GameSettings
import net.minecraft.util.ResourceLocation
import java.awt.Font
import java.io.File
import java.util.Arrays
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.toList

object CustomFontManager {
    private val fonts = ConcurrentHashMap<String, Font>()
    private val mc = Minecraft.getMinecraft()
    private val renderers = ConcurrentHashMap<FontCache, GameFontRenderer>()

    fun init() {
        loadFontsFromResources()
    }

    private fun readLinesFromStreams(vararg streams: java.io.InputStream?): List<String> {
        for (s in streams) {
            if (s != null) {
                return s.bufferedReader().use { it.readLines() }
            }
        }
        return emptyList()
    }

    private fun loadFontsFromResources() {
        try {
            val fontsListRes = ResourceLocation("manbou", "fonts.list")

            val resource = try {
                mc.resourceManager.getResource(fontsListRes)
            } catch (e: java.io.FileNotFoundException) {
                LogUtil.error("Critical: fonts.list not found in assets/manbou/! Did you forget to put it there?")
                return
            }

            val lines = resource.inputStream.bufferedReader().useLines { it.toList() }
            var any = false

            for (line in lines) {
                val t = line.trim()
                if (t.isEmpty() || t.startsWith("#")) continue

                val nameWithoutExt = t.substringBeforeLast('.')
                try {
                    val fontRes = ResourceLocation("manbou", "fonts/$t")
                    LogUtil.info("Loading font: assets/manbou/fonts/$t")

                    val fontInput = mc.resourceManager.getResource(fontRes).inputStream
                    val font = Font.createFont(Font.TRUETYPE_FONT, fontInput)

                    fonts[nameWithoutExt] = font
                    any = true
                    LogUtil.info("Successfully loaded: $nameWithoutExt")
                } catch (e: Exception) {
                    LogUtil.error("failed to load font file: $t    L", e)
                }
            }
            if (!any) LogUtil.warn("fonts.list was found, but no fonts were loaded.")
        } catch (e: Exception) {
            LogUtil.error("Lmao", e)
        }
    }

    fun getFont(name: String): Font? = fonts[name]

    fun getAvailableFonts(): List<String> = fonts.keys.toList()

    fun createCustomFontRenderer(fontName: String, size: Float): GameFontRenderer? {
        val cache = FontCache(fontName, size)
        if(renderers.containsKey(cache)) return renderers[cache]

        val font = fonts[fontName] ?: return null
        val derivedFont = font.deriveFont(size)

        return try {
            val renderer = GameFontRenderer(
                derivedFont
            )
            renderers[cache] = renderer
            renderer
        } catch (e: Exception) {
            LogUtil.error("Failed to create custom font renderer", e)
            null
        }
    }

    data class FontCache(val name: String, val size: Float)
}

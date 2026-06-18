package net.ax.manbou.module.impl.visual

import  net.ax.manbou.gui.ClickGuiScreen
import net.ax.manbou.module.*
import org.lwjgl.input.Keyboard

object ClickGui : ModuleBase(
    name = "ClickGui",
    category = Category.VISUAL,
    defaultState = false,
    forceEnable = false,
    shouldPlayToggleSound = true,
    keyBind = Keyboard.KEY_RSHIFT
) {
    override fun onEnable() {
        mc.displayGuiScreen(
            ClickGuiScreen()
        )
    }

    override fun onDisable() {
        if(mc.currentScreen is ClickGuiScreen) mc.displayGuiScreen(null)
    }
}

package net.ax.manbou

import net.ax.manbou.command.CommandManager
import net.ax.manbou.event.EventManager
import net.ax.manbou.font.CustomFontManager
import net.ax.manbou.module.ModuleManager
import net.ax.manbou.utils.IMinecraft
import net.ax.manbou.utils.LogUtil
import net.ax.manbou.utils.RotationManager
import java.security.SecureRandom

object Main: IMinecraft {
    val random = SecureRandom()

    fun launch() {
        LogUtil.info("Manbou initializing!")
        CustomFontManager.init()
        CommandManager.init()
        ModuleManager.init()
        EventManager.register(RotationManager)
    }
}
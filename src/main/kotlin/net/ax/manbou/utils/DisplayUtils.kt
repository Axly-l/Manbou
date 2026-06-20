package net.ax.manbou.utils

import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

object DisplayUtils: IMinecraft {
    const val PREFIX = "§bManbou:§r "

    fun addChatMessage(msg: String) {
        val component = ChatComponentText(msg)
        mc.ingameGUI.chatGUI.printChatMessage(component)
    }

    fun addClientMessage(msg: String) {
        addChatMessage(PREFIX + msg)
    }
}
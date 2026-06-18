package net.ax.manbou.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient

interface IMinecraft {
    val mc: Minecraft
        get() = Minecraft.getMinecraft()
}
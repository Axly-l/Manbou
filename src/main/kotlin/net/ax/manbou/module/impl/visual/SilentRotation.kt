package net.ax.manbou.module.impl.visual

import net.ax.manbou.module.*

object SilentRotation : ModuleBase("SilentRotation", Category.VISUAL) {
    val returnSpeed = FloatValue("ReturnSpeed", 30.0f, 0.0f..180.0f, "°")
}
package net.ax.manbou.module.impl.visual

import net.ax.manbou.module.BooleanValue
import net.ax.manbou.module.Category
import net.ax.manbou.module.ModuleBase

object FullBright: ModuleBase("FullBright", Category.VISUAL) {
    val noPumpkinBlur = BooleanValue("DisablePumpkinBlur", true)

    override fun onEnable() =
        mc.renderGlobal.loadRenderers()

    override fun onDisable() =
        mc.renderGlobal.loadRenderers()
}
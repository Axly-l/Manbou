package net.ax.manbou.module.impl.world

import net.ax.manbou.module.Category
import net.ax.manbou.module.IntValue
import net.ax.manbou.module.ModuleBase

object Boost: ModuleBase("Boost", Category.WORLD) {
    val tick = IntValue("Tick", 1, 0..10, "ticks")
    var boosting = false

    override fun onEnable() {
        boosting = true
    }

    override fun onDisable() {
        boosting = false
    }
}
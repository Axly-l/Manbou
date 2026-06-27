package net.ax.manbou.module.impl.world

import net.ax.manbou.module.Category
import net.ax.manbou.module.FloatValue
import net.ax.manbou.module.ModuleBase

object Timer: ModuleBase("Timer", Category.WORLD) {
    val speed = FloatValue("Speed", 1.0f, 0.1f..10.0f, "x", 0.01f) {
        if(this.state) mc.timer.timerSpeed = it
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1.0f
    }

    override fun onEnable() {
        mc.timer.timerSpeed = speed.value
    }
}
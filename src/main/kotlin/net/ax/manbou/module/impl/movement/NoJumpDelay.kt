package net.ax.manbou.module.impl.movement

import net.ax.manbou.event.EventTarget
import net.ax.manbou.event.Phase
import net.ax.manbou.event.SectionEvent
import net.ax.manbou.module.Category
import net.ax.manbou.module.ModuleBase

object NoJumpDelay: ModuleBase("NoJumpDelay", Category.MOVEMENT) {
    @EventTarget
    fun onSection(event: SectionEvent) {
        if(event.name == "jump") {
            mc.thePlayer?.jumpTicks = 0
        }
    }
}
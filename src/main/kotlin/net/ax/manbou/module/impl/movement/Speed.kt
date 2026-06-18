package net.ax.manbou.module.impl.movement

import com.sun.org.apache.xpath.internal.operations.Mult
import net.ax.manbou.event.EventTarget
import net.ax.manbou.event.StrafeEvent
import net.ax.manbou.module.Category
import net.ax.manbou.module.ModuleBase
import net.ax.manbou.module.MultiValue
import net.ax.manbou.utils.MovementUtils

object Speed: ModuleBase("Speed", Category.MOVEMENT) {
    val strafe = MultiValue("Strafe", listOf("Air", "Ground"), 0)

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val strafe = (strafe.isSelected("Air") && !mc.thePlayer.onGround) || (strafe.isSelected("Ground") && mc.thePlayer.onGround)
        if(state && strafe) MovementUtils.strafe(event)
    }
}
package net.ax.manbou.module.impl.combat

import net.ax.manbou.event.CameraEvent
import net.ax.manbou.event.EventTarget
import net.ax.manbou.event.TickEvent
import net.ax.manbou.module.*
import net.ax.manbou.utils.EntityUtils
import net.ax.manbou.utils.RotationManager
import net.ax.manbou.utils.RotationUtils
import net.ax.manbou.utils.TO_RADS
import net.minecraft.entity.EntityLivingBase
import kotlin.math.sin

object AimAssist: ModuleBase("AimAssist", Category.COMBAT) {
    val silent = BooleanValue("Silent", true)

    val range = DoubleValue("Range", 3.0, 2.0..10.0, "m")
    val angle = FloatValue("Angle", 60.0f, 0.0f..180.0f, "°")
    val wallCheck = BooleanValue("WallCheck", true)

    val rotationSpeed = FloatValue("RotationSpeed", 30.0f, 0.0f..300.0f)

    var targetIn: EntityLivingBase? = null

    @EventTarget
    fun onTick(event: TickEvent) {
        targetIn = EntityUtils.findTargets(range.value.toFloat(), angle.value, wallCheck.value).minByOrNull { mc.thePlayer.getDistanceToEntity(it) }
    }

    private var lastPT: Float = 0f

    @EventTarget
    fun onCamera(event: CameraEvent) {
        targetIn?.let {
            RotationManager.rotateEntity(0.15f * rotationSpeed.value, it, event.partialTicks, lastPT, silent.value)
        }
        lastPT = event.partialTicks
    }
}